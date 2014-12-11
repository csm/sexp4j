# S-expressions for Java.

[![Travis Status](https://travis-ci.org/csm/sexp4j.svg?branch=master)](https://travis-ci.org/csm/sexp4j)

[![Clojars Project](http://clojars.org/org.metastatic/sexp4j/latest-version.svg)](http://clojars.org/org.metastatic/sexp4j)

This package implements Ron Rivest's [s-expression](http://people.csail.mit.edu/rivest/Sexp.txt)
format. It also provides some useful ways to use this format for serializing Java objects.

# Rationale

JSON is great. It's a simple, readable format that encodes basic objects very well; it's somewhat compact, and somewhat fast to parse. It's implemented in any language you can think of. For a ton of uses, JSON is the perfect answer.

But we can do better, if our data are more binary than text, and if we want to have an unambiguous representation of a single data item. There are other binary formats, but I think they tend to lack the regularity, simplicity, and hacker-readability (if not human-readability) of JSON. S-expressions (the format, not the concept from Lisps, which is very different) solved this a long time ago, in (I think) a better way than other formats have.

* Protocol buffers are pretty great, I like them a lot. However, they're highly tied to schemas, and don't really represent collections well.
* BSON made some weird decisions about encoding, making it more awkward than I would have liked.
* MessagePack looks pretty good, but some of the things in it seemed to make it grow more heads than it should have.

Like JSON, you can describe the s-expression format succinctly:

> Data encoded in an s-expression is a sequence of expressions; an expression is either an atom, which is a fixed-size sequence of bytes, optionally preceded by a display hint; or it is a list, which is a open parenthesis `(`, followed by zero or more simpler expressions, and ended by a closing parenthesis `)`. A display hint is a single atom preceded by a open bracket `[` and followed by a close bracket `]`.

From there, you also have two encodings:

* Canonical, which is what we use most of the time, because it is unambiguous and compact. Atoms are encoded as "net strings", beginning with a decimal length, followed by a colon, and then followed by that many bytes; e.g., `5:hello`. The rest of the encoding is pretty obvious, since it is just delimiters; whitespace can be optional between elements.
* Advanced, which is more for humans to read; atoms can be net strings, quoted strings, plain unquoted symbols, hexadecimal, or base-64. Whitespace and newlines are allowed, and encouraged for readability.

# A "simple" object mapping

The idea behind a "simple" mapper is to make a simple serialization format for primitives and basic collections. It works as follows:

* Primitives are encoded as atoms, with a one-byte display hint to mark the type:
    * Null values have display hint `n`, and the value is empty.
    * Booleans have a display hint `z`, and a one-byte value zero (false) or non zero (true, should typically be the value 1).
    * Bytes have display hint `b`, encoded as-is.
    * Shorts have display hint `s`, encoded as two bytes, big-endian, two's complement. All multibyte integers are encoded as big-endian, two's complement values.
    * Chars have display hint `c`, encoded as two bytes, unsigned, big-endian.
    * Integers have display hint `i`, four bytes.
    * Longs display hint `l`, eight bytes.
    * Floats display hint `f`, four bytes, encoded as with [floatToIntBits](http://docs.oracle.com/javase/7/docs/api/java/lang/Float.html#floatToIntBits%28float%29).
    * Doubles display hint `d`, eight bytes, encoded as with [doubleToLongBits](http://docs.oracle.com/javase/7/docs/api/java/lang/Double.html#doubleToLongBits%28double%29).
    * BigIntegers display hint `I`, encoded as with [toByteArray](http://docs.oracle.com/javase/7/docs/api/java/math/BigInteger.html#toByteArray%28%29).
    * BigDecimals display hint `D`, encoded as a UTF-8 string, as from [toString](http://docs.oracle.com/javase/7/docs/api/java/math/BigDecimal.html#toString%28%29).
    * Strings display hint `S`, encoded as UTF-8.
* Collections are always encoded as a list, with a one-byte atom as the first element, giving the type of collection.
    * Lists will have type `l`, followed by the contents of the list, in order. The length of the encoded list will therefore be one larger than the length of the underlying list.
    * Sets will have type `s`, followed by the contents of the set, in iteration order. Deserializing by this library will preserve the order of elements as they appear (it uses a LinkedHashMap underneath).
    * Maps will have type `m`, followed by each key and value. Each key has no display hint, and is taken to be a string encoded as UTF-8. Each value is encoded according to this format. Thus the encoded list will have twice the number of elements of the source map, plus one.
    
Some examples:

    null -> [n]""
    true, false -> [z]#01# [z]#00#
    byte 0xab -> [b]#ab#
    short 42 -> [s]#002a#
    int 65526 -> [i]#00010000#
    long 4294967296 -> [l]#00000100000000#
    float 3.141 -> [f]#40490625#
    double 2.718281828459045 -> [d]#4005bf0a8b145769#
