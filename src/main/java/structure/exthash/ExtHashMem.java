/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package structure.exthash;

import static java.lang.String.format;
import static java.lang.System.arraycopy;
import static java.util.Arrays.binarySearch;
import static org.apache.jena.atlas.lib.Alg.decodeIndex;

import java.util.Arrays;

import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.lib.BitsLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Extensible hashing
 * http://en.wikipedia.org/wiki/Extendible_hashing
 */
public class ExtHashMem<K,V>
{
    // Purely in-memory version (for comparison and understanding)

    /* Hashing.
     * Extendible hashing is based on taking more of the bits of the hash
     * value to address an expanding dictionary.  This is a bit-trie, stored
     * as an array.  One bucket can be used for several hash slots.
     *
     * We need that the bits are used in decreasing signifance because we
     * keep items in buckets in full-hash sorted order.
     *
     * Side effect: the whole structure is sorted by full hash, using
     * dictionary and buckets.
     *
     * But.
     * Java .hashCode() does not make suitable hash directly because either
     * they are Object.hashCode (not too bad but it tends not to use high bits) or
     * something like Integer.hashCode is the integer value itself.  The
     * latter is very bad as the hash is not using the high bits (most
     * integers are small - especially sequentially allocated numbers).
     *
     * Solution: use the hashCode, 31 bits (arrays indexes are signed)
     * but bit reversed so low bits of the original value are the most
     * significant.
     *
     * All hash handling is encapulated in the internal routines.
     */

    static private Logger log = LoggerFactory.getLogger(ExtHashMem.class) ;

    // Production: make these final and false.
    public static boolean NullOut = true ;          // Set released space to something not an valid entry.
    public static boolean Checking = false ;        // Perform internal checking
    public static boolean Logging = false ;         // Allow any logging code on critical paths

    // Give each bucket a unique-per-hashtable id.  Debugging.
    private int bucketCounter = 0 ;

    // Dictionary - can't have Java Arrays of generics.  Could use List<>
    private Object[] dictionary ;      // mask(hash) -> Bucket

    // Current length of trie bit used.  Invariant: dictionary.length = 1<<bitLen
    private int bitLen = 0 ;
    // Number of things in the hash table.
    private long size = 0 ;

    public static int DefaultBucketSize = 10 ;
    // Size of bucket
    final int bucketSize ;


    public ExtHashMem() { this(DefaultBucketSize) ; }

    public ExtHashMem(int bucketSize) { this(bucketSize, 0) ; }

    public ExtHashMem(int bucketSize, int initialBitLength)
    {
        this.bucketSize = bucketSize ;
        int dictionarySize = 1<<initialBitLength ;
        dictionary = new Object[dictionarySize] ;
        Bucket<V> bucket = new Bucket<V>(0, initialBitLength, bucketSize, bucketCounter++) ;
        dictionary[0] = bucket ;
        bitLen = 0 ;
    }

    // =====================
    // Hashing routines for converting to a bit-trie (i.e. highest bit
    // is most significant in the trie).

    //From java.util.HashMap
//  /**
//   * Applies a supplemental hash function to a given hashCode, which
//   * defends against poor quality hash functions.  This is critical
//   * because HashMap uses power-of-two length hash tables, that
//   * otherwise encounter collisions for hashCodes that do not differ
//   * in lower bits. Note: Null keys always map to hash 0, thus index 0.
//   */
//  static int hash(int h) {
//      // This function ensures that hashCodes that differ only by
//      // constant multiples at each bit position have a bounded
//      // number of collisions (approximately 8 at default load factor).
//      h ^= (h >>> 20) ^ (h >>> 12);
//      return h ^ (h >>> 7) ^ (h >>> 4);
//  }

    // The bit reverse of .hashCode so that integers are spread out.
    // It's important that bits "appear" as the length changes in decreasing
    // significance order.
    // Use 31 bits (not the top hashCode bit) because array indexes are 32bit.

    /** Turn a key into a bit trie has value */
    protected static <K> int keyHash(K k)             { return Integer.reverse(k.hashCode())>>>1 ; }

    /** Calculate the array index for a key given the dictionary bit length */
    protected static <K> int index(K k, int bitLen)    { return index(keyHash(k), bitLen) ; }

    /** Convert from full hash to array index for a dictionary bit length */
    protected static <K> int index(int fullHash, int bitLen)
    {
        //return (int)Bits.unpack(fullHash, (31-bitLen), 31) ;
        return fullHash >>> (31-bitLen) ;
    }

    // =====================

    private void resizeDictionary()
    {
        int oldSize = 1<<bitLen ;
        int newBitLen = bitLen+1 ;
        int newSize = 1<<newBitLen ;

        if ( Logging && log.isDebugEnabled() )
        {
            log.debug(format("resize: %d  ==> %d", oldSize, newSize)) ;
            System.out.println(">>>>Resize") ;
            System.out.println(this) ;
            System.out.println("----Resize") ;
        }

        Object[] newDictionary = new Object[newSize] ;
        if ( dictionary != null )
        {
            // Fill new dictionary
            // NB Fills from high to low so that it works if done "in place"
            for ( int i = oldSize-1 ; i>=0 ; i-- )
            {
                newDictionary[2*i] = dictionary[i] ;
                newDictionary[2*i+1] = dictionary[i] ;
            }
        }
        dictionary = newDictionary ;
        bitLen = newBitLen ;
        if ( Logging && log.isDebugEnabled() )
        {
            System.out.println(this) ;
            System.out.println("<<<<Resize") ;
        }
        internalCheck() ;
    }

    //@Override
    public boolean contains(K key)
    {
        return get(key) != null ;
    }

    //@Override
    public V get(K key)
    {
        if ( Logging && log.isDebugEnabled() )
            log.debug(format(">> get(%s)", key)) ;
        int i = index(key, bitLen) ;
        @SuppressWarnings("unchecked")
        Bucket<V> bucket =  (Bucket<V>)dictionary[i] ;
        // Maybe multiples
        V value = bucket.find(keyHash(key)) ;
        if ( Logging && log.isDebugEnabled() )
            log.debug(format(">> get(%s) -> %s", key, value)) ;
        return value ;
    }

    //@Override
    public void put(K key, V value)
    {
        if ( Logging && log.isDebugEnabled() )
            log.debug(format(">> put(%s,%s)", key, value)) ;
        int h = keyHash(key) ;
        boolean b = _put(key, value, h) ;
        if ( b )
            size++ ;
        internalCheck() ;
        if ( Logging && log.isDebugEnabled() )
        {
            log.debug(format("<< put(%s,%s)", key, value)) ;
            dump() ;
        }
    }

    //@Override
    public void remove(K key)
    {
        if ( Logging && log.isDebugEnabled() )
            log.debug(format(">> remove(%s)", key)) ;
        int hash = keyHash(key) ;
        int i = index(hash, bitLen) ;
        @SuppressWarnings("unchecked")
        Bucket<V> bucket =  (Bucket<V>)dictionary[i] ;
        boolean b = bucket.removeByHash(hash) ;
        if ( b )
            size-- ;
        internalCheck() ;
        if ( Logging && log.isDebugEnabled() )
            log.debug(format("<< remove(%s)", key)) ;
    }

    //@Override
    public long size()
    {
        return size ;
    }

    //@Override
    public void sync()
    {}

    //@Override
    public void close()
    {}

    // =====================
    // Insert

    // Reentrant part of "put"
    private boolean _put(K key, V value, int hash)
    {
        if ( Logging && log.isDebugEnabled() )
            log.debug(format("put(%s,%s,0x%08X)", key, value, hash)) ;

        int idx = index(hash, bitLen) ;
        @SuppressWarnings("unchecked")
        Bucket<V> bucket =  (Bucket<V>)dictionary[idx] ;

        if ( bucket.hasRoom() )
        {
            boolean b = bucket.put(hash, value) ;
            return b ;
        }

        // Bucket full.
        if (  bitLen == bucket.bucketBitLen )
        {
            // Bucket not splitable..
            // TODO Overflow buckets.
            // Expand the dictionary.
            resizeDictionary() ;
            // Try again
            return _put(key, value, hash) ;
        }


        // bitLen >  bucket.bucketBitLen : bucket can be split
        splitAndReorganise(bucket, idx, hash) ;

        // Reorg done - try again.
        return _put(key, value, hash) ;
    }


    private void splitAndReorganise(Bucket<V> bucket, int idx, int hash)
    {
        if ( Checking && log.isDebugEnabled() )
        {
            log.debug(format("splitAndReorganise: idx=%d, bitLen=%d, bucket.hashLength=%d",
                             idx, bitLen, bucket.bucketBitLen)) ;
            dump() ;
        }

        if ( Checking && index(hash, bucket.bucketBitLen) != bucket.hash )
            error("splitAndReorganise: idx=0x%X : hash=0x%X[0x%X,0x%X] : Inconsistency : %s",
                  idx, hash, index(hash, bucket.bucketBitLen), bucket.hash, bucket) ;

        // Bucket did not have a full length hash so split it.
        // Find the companion slots.
        // Remember before messing with split.
        int bucketHash = bucket.hash ;
        int bucketHashLength = bucket.bucketBitLen ;

        // Split the bucket in two.  buckets2 is the upper bucket.
        Bucket<V> bucket2 = split(idx, bucket) ;

        // Determine the slots affected: all the dictionary entries that
        // correspond to the extension of the but trie by a 0x1 in the next bit.
        // A bitLen length bit pattern, with
        // Top:     bucket hash
        //          1 (the ones with 0 are left with the original (and now smaller) bucket
        // Bottom:  All possible bits in lower bits
        int trieUpperRoot = ((bucketHash<<1)|0x1) << (bitLen-bucketHashLength-1) ;
        int trieUpperRange = (1<<(bitLen-bucketHashLength-1)) ;

        for ( int j = 0 ; j < trieUpperRange ; j++ )
        {
            // j runs over the values of the unused bits of the trie start for the upper bucket positions.
            int k = trieUpperRoot | j ;
            if ( Checking )
            {
                if ( (trieUpperRoot&j) != 0 )
                    error("put: idx=%d : trieRoot=0x%X, sub=%d: Broken trie pattern ", idx, trieUpperRoot, j) ;
                if ( ! BitsLong.isSet(k, (bitLen-(bucketHashLength+1)) ) )
                    error("put: Broken trie pattern (0x%X,%d)", trieUpperRoot, j) ;
                if ( dictionary[k] != bucket )
                    error("put: Wrong bucket at trie (0x%X,%d)", trieUpperRoot, j) ;
            }
            dictionary[k] = bucket2 ;
        }
        if ( Logging && log.isDebugEnabled() )
        {
            log.debug("Reorg complete") ;
            //dump() ;
        }

    }

    private Bucket<V> split(int idx, Bucket<V> bucket)
    {
        // idx is the array offset to the lower of the bucket point pair.
        if ( Logging && log.isDebugEnabled() )
        {
            log.debug(format("split: Bucket %d : size: %d; Bucket bitlength %d", idx, bucket.size, bucket.bucketBitLen)) ;
            log.debug(format("split: %s", bucket)) ;
        }

        // Create new bucket, which will be the upper bucket.
        // Low bucket will have the old hash value,
        // lengthen the hash;
        // The new will be one more.


        bucket.bucketBitLen++ ;

        int hash1 = bucket.hash << 1 ;
        int hash2 = (bucket.hash << 1) | 0x1 ;

        bucket.hash = hash1 ;
        //log.debug(format("split: bucket hashes 0x%X 0x%X", hash1, hash2)) ;

        Bucket<V> bucket2 = new Bucket<V>(hash2, bucket.bucketBitLen, bucketSize, bucketCounter++) ;

        if ( Logging && log.isDebugEnabled() )
        {
            log.debug(format("split: old bucket %s", bucket)) ;
            log.debug(format("split: new bucket %s", bucket2)) ;
        }

        // Split value is where hash2 starts - scaled up to align to full key length
        int x = hash2 << (31-bucket.bucketBitLen) ;

        // We keep buckets sorted by hash so revealing a new lower bit is
        // finding by some split value.
        int split = bucket.findIndex(x) ;
        if ( split < 0 )
            split = decodeIndex(split) ;

        arraycopy(bucket.keys,  split, bucket2.keys,  0, bucket.size-split) ;
        arraycopy(bucket.items, split, bucket2.items, 0, bucket.size-split) ;

        if ( NullOut )
        {
            Arrays.fill(bucket.keys, split, bucket.size, -1) ;
            Arrays.fill(bucket.items, split, bucket.size, null) ;
        }
        bucket2.size = bucket.size-split ;
        bucket.size = split ;
        if ( Logging && log.isDebugEnabled() )
        {
            log.debug(format("split: bucket  %s", bucket)) ;
            log.debug(format("split: bucket2 %s", bucket2)) ;
        }
        return bucket2 ;
    }

    // =====================

    @Override
    public String toString()
    {
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        dump(buff) ;
        return buff.asString() ;
    }

    public void dump()
    {
        dump(IndentedWriter.stdout) ;
        IndentedWriter.stdout.ensureStartOfLine() ;
        IndentedWriter.stdout.flush() ;
    }

    private void dump(IndentedWriter out)
    {
        out.printf("Bitlen      = %d \n" , bitLen) ;
        out.printf("Dictionary  = %d \n" , 1<<bitLen ) ;
        out.incIndent(4) ;
        for ( int i = 0 ; i < 1<<bitLen ; i++ )
        {
            out.ensureStartOfLine() ;
            @SuppressWarnings("unchecked")
            Bucket<V> bucket = (Bucket<V>)dictionary[i] ;
            out.printf("%02d ", i) ;
            out.printf(bucket.toString()) ;
        }
        out.decIndent(4) ;
    }

    public void check()
    {
        performCheck() ;
    }

    private final void internalCheck()
    {
        if ( Checking )
            performCheck() ;
    }

    private final void performCheck()
    {
        int len = 1<<bitLen ;
        if ( len != dictionary.length )
            error("Dictionary size = %d : expected = %d", dictionary.length, len) ;
        Bucket<V> prevBucket = null ;
        for ( int i = 0 ; i < len ; i++ )
        {
            @SuppressWarnings("unchecked")
            Bucket<V> bucket = (Bucket<V>)dictionary[i] ;
            if ( prevBucket != bucket )
            {
                performCheck(i, bucket) ;
                prevBucket = bucket ;
            }
        }
    }

    private void performCheck(int idx, Bucket<V> bucket)
    {
        if ( bucket.keys.length != bucket.items.length )
            error("Bucket %d arrays of different sizes (keys=%d, items=%d)", idx, bucket.keys.length, bucket.items.length) ;
        if ( bucket.bucketBitLen > bitLen )
            error("Bucket %d has bit length longer than the dictionary's (%d, %d)", bucket.bucketBitLen, bitLen) ;

        // Check the bucket hash against the slot it's in.
        // Convert directory index to bucket hash
        int tmp = (idx >>> (bitLen-bucket.bucketBitLen)) ;
        if ( tmp != bucket.hash)
            error("Bucket %d : hash prefix 0x%X, expected 0x%X : %s", idx, bucket.hash, tmp, bucket) ;

        // Check the contents.
        int prevKey = Integer.MIN_VALUE ;
        for ( int i = 0 ; i < bucket.size ; i++ )
        {
            if ( bucket.keys[i] < prevKey )
                error("Bucket %d: Not sorted (slot %d) : %s", idx, i, bucket) ;
            prevKey = bucket.keys[i] ;
            int x = index(bucket.keys[i], bucket.bucketBitLen) ;
            // Check the key is bucket-compatible.
            if ( x != bucket.hash )
                error("Bucket %d: Key (0x%08X[0x%X]) does not match the hash (0x%X) : %s",
                             idx, bucket.keys[i], x, bucket.hash, bucket) ;
        }

        if ( NullOut )
        {
            for ( int i = bucket.size ; i < bucket.keys.length ; i++ )
            {
                if ( bucket.items[i] != null )
                    error("Bucket %d : overspill in items[%d]", idx, i) ;
                if ( bucket.keys[i] != -1 )
                    error("Bucket %d : overspill in keys[%d]", idx, i) ;
            }
        }
    }

    private void error(String msg, Object... args)
    {
        msg = format(msg, args) ;
        System.out.println("**************") ;
        System.out.println(msg) ;
        System.out.println() ;
        System.out.flush();
        try {
            System.out.println(this) ;
        } catch (Exception ex) {}
        throw new RuntimeException(msg) ;
    }

    static final class Bucket<V>
    {
        // Don't assume unique key

        // List<V> items = new ArrayList<V>() ;
        // Parallel arrays.

        // Keep sorted for faster find.

        protected final int[] keys ;
        protected final Object[] items ;

        protected final int id ;
        protected int hash ;
        // How many bits are used for storing in this bucket.
        protected int bucketBitLen ;
        protected int size ;

        Bucket(int hashValue, int bucketBitLen, int maxSize, int bucketId)
        {
            id = bucketId ;
            keys = new int[maxSize] ;
            if ( NullOut )
                Arrays.fill(keys, -1) ;
            items = new Object[maxSize] ;
            size = 0 ;
            this.bucketBitLen = bucketBitLen ;
            this.hash = hashValue ;
        }

        // Find the index of the key, return insertion a point if not found as -(i+1)
        final int findIndex(int key)
        {
            int i = binarySearch(keys, 0, size, key) ;
            return i ;
        }

        final boolean hasRoom()
        {
            return size < keys.length ;
        }

        // Return true is added a new value
        final boolean put(int key, V v)
        {
            int i = findIndex(key) ;
            if ( i < 0 )
                i = decodeIndex(i) ;
            else
            {
                @SuppressWarnings("unchecked")
                V v2 = (V)items[i] ;
                if ( v2.equals(v) )
                    return false ;
                i++ ;
            }

            if ( ! hasRoom() )
                throw new RuntimeException("Bucket overflow") ;

            for ( int j = size ; j > i ; j-- )
            {
                keys[j] = keys[j-1] ;
                items[j] = items[j-1] ;
            }
            keys[i] = key ;
            items[i] = v ;
            size++ ;
            return true ;
        }

        final boolean removeByHash(int key)
        {
            int i = findIndex(key) ;
            if ( i < 0 )
                return false ;
            if ( i < size-1 )
            {
                System.arraycopy(keys, i+1, keys, i, size-i-1) ;
                System.arraycopy(items, i+1, items, i, size-i-1) ;
            }
            size -- ;
            if ( NullOut )
            {
                keys[size] = -1 ;
                items[size] = null ;
            }
            return true ;
        }

        // Return the item in slot idx
        final V get(int key, int idx)
        {
            if ( idx >= items.length )
                return null ;
            int k = keys[idx] ;
            if ( k != key )
                return null ;
            @SuppressWarnings("unchecked")
            V v = (V)items[idx] ;
            return v ;
        }

        final V find(int key)
        {
            int i = findIndex(key) ;
            if ( i < 0 )
                return null ;
            @SuppressWarnings("unchecked")
            V v = (V)items[i] ;
            return v ;
        }

        @Override
        public String toString()
        {
            StringBuilder buff = new StringBuilder() ;
            buff.append(format("<<[id=%02d, size=%s, len=%d, hash=0x%08X] ", id, size, bucketBitLen, hash)) ;
            for ( int i = 0 ; i < size ; i++ )
            {
                if ( i != 0 )
                    buff.append(" ") ;
                buff.append(format("(0x%X/0x%08X,%s)",
                                   index(keys[i], bucketBitLen),
                                   keys[i],
                                   items[i].toString())) ;

            }
            buff.append(">>") ;
            return buff.toString() ;

        }
    }
}
