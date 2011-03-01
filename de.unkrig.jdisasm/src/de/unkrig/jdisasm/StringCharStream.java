
/*
 * JDISASM - A Java[TM] class file disassembler
 *
 * Copyright (c) 2001-2011, Arno Unkrig
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *       following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 *       following disclaimer in the documentation and/or other materials provided with the distribution.
 *    3. The name of the author may not be used to endorse or promote products derived from this software without
 *       specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package de.unkrig.jdisasm;

import java.io.EOFException;
import java.io.IOException;

public class StringCharStream implements CharStream {

    private final String in;
    private int          idx;

    public StringCharStream(String in) {
        this.in = in;
    }

    public boolean peek(char c) {
        return idx < in.length() && in.charAt(idx) == c;
    }

    public char read() throws EOFException {
        if (idx == in.length()) throw new EOFException("Unexpected end-of-input");
        return in.charAt(idx++);
    }

    public void read(char c) throws EOFException, IOException {
        if (idx == in.length()) throw new EOFException("Expected '" + c + "' instead of end-of-input");
        if (in.charAt(idx) != c) throw new IOException("'" + c + "' expected instead of '" + in.substring(idx) + "'");
        idx++;
    }

    public boolean peekRead(char c) {
        if (idx >= in.length()) return false;
        if (in.charAt(idx) == c) {
            idx++;
            return true;
        }
        return false;
    }

    public void eos() throws IOException {
        if (idx < in.length()) throw new IOException("Unexpected trailing characters '" + in.substring(idx) + "'");
    }

    public boolean peekEos() {
        return idx >= in.length();
    }

    public char peek() throws EOFException {
        if (idx == in.length()) throw new EOFException("Unexpected end-of-input");
        return in.charAt(idx);
    }

    public int peek(String chars) {
        return idx == in.length() ? -1 : chars.indexOf(in.charAt(idx));
    }

    public int read(String chars) throws EOFException, IOException {
        if (idx == in.length()) throw new EOFException("Expected one of '" + chars + "' instead of end-of-input");
        int res = chars.indexOf(in.charAt(idx));
        if (res == -1) throw new IOException("One of '" + chars + "' expected instead of '" + in.charAt(idx) + "'");
        idx++;
        return res;
    }

    public String toString() {
        return "'" + in + "' at offset " + idx;
    }
}