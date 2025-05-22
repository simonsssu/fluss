/*
 *  Copyright (c) 2025 Alibaba Group Holding Ltd.
 *
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
 */

package com.alibaba.fluss.cli.format.style;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.StringUtils;

/**
 * Utility class for printing formatted tables to console output.
 *
 * <p>Example output:
 *
 * <pre>
 * +------------+-------+
 * | Header1    | Hdr2  |
 * +------------+-------+
 * | Data       | Value |
 * +------------+-------+
 * </pre>
 */
public class TablePrinter {

    public static void printTable(List<String> headers, List<List<String>> data) {
        List<List<String>> rows =
                data.stream()
                        .map(
                                row ->
                                        row.stream()
                                                .map(cell -> cell != null ? cell : "")
                                                .collect(Collectors.toList()))
                        .collect(Collectors.toList());

        List<Integer> colWidths =
                IntStream.range(0, headers.size())
                        .mapToObj(
                                i -> {
                                    int headerLen = headers.get(i).length();
                                    int maxDataLen =
                                            rows.stream()
                                                    .mapToInt(row -> row.get(i).length())
                                                    .max()
                                                    .orElse(0);
                                    return Math.max(headerLen, maxDataLen);
                                })
                        .collect(Collectors.toList());

        String sepLine = formatLine(colWidths, true);
        String rowFormat = formatLine(colWidths, false);

        System.out.println(sepLine);
        System.out.printf(rowFormat, headers.toArray());
        System.out.println(sepLine);
        for (List<String> row : rows) {
            System.out.printf(rowFormat, row.toArray());
        }
        System.out.println(sepLine);
    }

    private static String formatLine(List<Integer> widths, boolean isSeparator) {
        StringBuilder sb = new StringBuilder();
        for (int width : widths) {
            sb.append(
                    isSeparator
                            ? "+" + StringUtils.repeat("-", width + 2)
                            : String.format("| %%-%ds ", width));
        }
        sb.append(isSeparator ? "+" : "|\n");
        return sb.toString();
    }
}
