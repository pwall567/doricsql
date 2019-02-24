/*
 * @(#) SQLProcessor.java
 *
 * doric Column-oriented database system
 * Copyright (c) 2019 Peter Wall
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.pwall.doric.sql;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.pwall.doric.Row;

public class SQLProcessor {

    public void process(Reader rdr) throws IOException {
        SQLParseText spt = new SQLParseText(rdr);
        for (;;) {
            spt.skipSpacesMultiLine();
            if (spt.isExhausted())
                break;
            if (spt.matchName("select"))
                processSelect(spt);
            else
                throw new IllegalArgumentException("SQL syntax error: " + spt);
        }

    }

    private SQLQuery processSelect(SQLParseText spt) throws IOException {

        List<SQLQueryColumn> columnEntries = new ArrayList<>();

        for (;;) {
            spt.skipSpacesMultiLine();
            String resultName = null;
            SQLExpression expression;
            if (spt.matchDec()) {
                expression = new SQLConstant(spt.getResultLong());
            }
            else if (spt.match('\'')) {
                StringBuilder sb = new StringBuilder();
                for (;;) {
                    if (spt.match('\'')) {
                        if (!spt.match('\''))
                            break;
                    }
                    spt.appendResultTo(sb);
                }
                expression = new SQLConstant(sb.toString());
            }
            else if (spt.matchName()) {
                String tableName = null;
                String columnName = spt.getResultString();
                spt.skipSpacesMultiLine();
                // check for '(' - indicates function call
                if (spt.match('.')) {
                    tableName = columnName;
                    spt.skipSpacesMultiLine();
                    if (!spt.matchName())
                        throw new IllegalArgumentException("SQL syntax error: " + spt);
                    columnName = spt.getResultString();
                }
                expression = new SQLVariable(tableName, columnName);
                resultName = columnName;
            }
            else
                throw new IllegalArgumentException("SQL syntax error: " + spt);
            spt.skipSpacesMultiLine();
            if (spt.matchName("as")) {
                spt.skipSpacesMultiLine();
                if (!spt.matchName())
                    throw new IllegalArgumentException("SQL syntax error: " + spt);
                resultName = spt.getResultString();
                spt.skipSpacesMultiLine();
            }
            columnEntries.add(new SQLQueryColumn(resultName, expression));
            if (spt.isExhausted())
                return new SimpleQuery(columnEntries);
            if (spt.matchName("from"))
                break;
            if (!spt.match(','))
                throw new IllegalArgumentException("SQL syntax error: " + spt);
        }
        spt.skipSpacesMultiLine();

        // we have just passed the "from" token


        return null; // TEMPORARY
    }

    public static class SimpleQuery extends SQLQuery {

        public SimpleQuery(List<SQLQueryColumn> columnEntries) {
            super(columnEntries);
        }

        @Override
        public Iterator<Row> execute() {
            return null;
        }

    }

    public static class TableQuery extends SQLQuery {

        private String tableName;

        public TableQuery(String tableName, List<SQLQueryColumn> columnEntries) {
            super(columnEntries);
            this.tableName = tableName;
        }

        @Override
        public Iterator<Row> execute() {
            // iterator must iterate through table(s), apply join(s), where clause, group by and order by
            // for each joined row, it must then create a new Row object using column entries
            return null;
        }

        // we probably need another method - get column metadata based on column entries

        // and instead of putting all the effort into parsing SQL, perhaps we could just create the
        // query objects directly?

        // also - add other getxxx methods to Row

        // and move all the SQL stuff to a separate project

    }

}
