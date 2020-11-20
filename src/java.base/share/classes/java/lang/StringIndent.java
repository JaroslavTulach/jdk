/*
 * Copyright (c) 1994, 2020, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.lang;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class StringIndent {
    static String stripIndent(String thiz) {
        int length = thiz.length();
        if (length == 0) {
            return "";
        }
        char lastChar = thiz.charAt(length - 1);
        boolean optOut = lastChar == '\n' || lastChar == '\r';
        List<String> lines = thiz.lines().collect(Collectors.toList());
        final int outdent = optOut ? 0 : outdent(lines);
        return lines.stream()
                .map(line -> {
                    int firstNonWhitespace = indexOfNonWhitespace(line);
                    int lastNonWhitespace = lastIndexOfNonWhitespace(line);
                    int incidentalWhitespace = Math.min(outdent, firstNonWhitespace);
                    return firstNonWhitespace > lastNonWhitespace
                            ? "" : line.substring(incidentalWhitespace, lastNonWhitespace);
                })
                .collect(Collectors.joining("\n", "", optOut ? "\n" : ""));
    }

    private static int outdent(List<String> lines) {
        // Note: outdent is guaranteed to be zero or positive number.
        // If there isn't a non-blank line then the last must be blank
        int outdent = Integer.MAX_VALUE;
        for (String line : lines) {
            int leadingWhitespace = indexOfNonWhitespace(line);
            if (leadingWhitespace != line.length()) {
                outdent = Integer.min(outdent, leadingWhitespace);
            }
        }
        String lastLine = lines.get(lines.size() - 1);
        if (lastLine.isBlank()) {
            outdent = Integer.min(outdent, lastLine.length());
        }
        return outdent;
    }

    static int indexOfNonWhitespace(String line) {
        return line.isLatin1() ? StringLatin1.indexOfNonWhitespace(line.value())
                          : StringUTF16.indexOfNonWhitespace(line.value());
    }


    static int lastIndexOfNonWhitespace(String line) {
        return line.isLatin1() ? StringLatin1.lastIndexOfNonWhitespace(line.value())
                          : StringUTF16.lastIndexOfNonWhitespace(line.value());
    }

    static String indent(String thiz, int n) {
        if (thiz.isEmpty()) {
            return "";
        }
        Stream<String> stream = thiz.lines();
        if (n > 0) {
            final String spaces = " ".repeat(n);
            stream = stream.map(s -> spaces + s);
        } else if (n == Integer.MIN_VALUE) {
            stream = stream.map(s -> s.stripLeading());
        } else if (n < 0) {
            stream = stream.map(s -> s.substring(Math.min(-n, indexOfNonWhitespace(thiz))));
        }
        return stream.collect(Collectors.joining("\n", "", "\n"));
    }

}
