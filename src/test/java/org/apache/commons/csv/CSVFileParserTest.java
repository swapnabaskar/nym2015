/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Parse tests using test files
 *
 * @version $Id$
 */
@RunWith(Parameterized.class)
public class CSVFileParserTest {

    private static final File BASE = new File("src/test/resources/CSVFileParser");

    private final BufferedReader testData;
    private final String testName;

    public CSVFileParserTest(final File file) throws FileNotFoundException
    {
       this.testName = file.getName();
       this.testData = new BufferedReader(new FileReader(file));
    }

    private String readTestData() throws IOException {
        String line;
        do {
            line = testData.readLine();
        } while (line != null && line.startsWith("#"));
        return line;
    }

    @Parameters
    public static Collection<Object[]> generateData()
    {
        final List<Object[]> list = new ArrayList<Object[]>();

        final FilenameFilter filenameFilter = new FilenameFilter() {
            public boolean accept(final File dir, final String name) {
                return name.startsWith("test") && name.endsWith(".txt");
            }
        };
        final File[] files = BASE.listFiles(filenameFilter);
        for(final File f : files){
            list.add(new Object[]{f});
        }
        return list;
    }

    @Test
    public void testCSVFile() throws Exception {
        String line = readTestData();
        assertNotNull("file must contain config line", line);
        final String[] split = line.split(" ");
        assertTrue(testName+" require 1 param", split.length >= 1);
         // first line starts with csv data file name
        CSVFormat format = CSVFormat.newFormat(',').withQuoteChar('"');
        boolean checkComments = false;
        for(int i=1; i < split.length; i++) {
            final String option = split[i];
            final String[] option_parts = option.split("=",2);
            if ("IgnoreEmpty".equalsIgnoreCase(option_parts[0])){
                format = format.withIgnoreEmptyLines(Boolean.parseBoolean(option_parts[1]));
            } else if ("IgnoreSpaces".equalsIgnoreCase(option_parts[0])) {
                format = format.withIgnoreSurroundingSpaces(Boolean.parseBoolean(option_parts[1]));
            } else if ("CommentStart".equalsIgnoreCase(option_parts[0])) {
                format = format.withCommentStart(option_parts[1].charAt(0));
            } else if ("CheckComments".equalsIgnoreCase(option_parts[0])) {
                checkComments = true;
            } else {
                fail(testName+" unexpected option: "+option);
            }
        }
        line = readTestData(); // get string version of format
        assertEquals(testName+" Expected format ", line, format.toString());

        // Now parse the file and compare against the expected results
        // We use a buffered reader internally so no need to create one here.
        CSVParser parser = CSVParser.parseFile(new File(BASE, split[0]), format);
        for(final CSVRecord record : parser) {
            String parsed = record.toString();
            if (checkComments) {
                final String comment = record.getComment().replace("\n", "\\n");
                if (comment != null) {
                    parsed += "#" + comment;
                }
            }
            final int count = record.size();
            assertEquals(testName, readTestData(), count+":"+parsed);
        }
    }

    @Test
    public void testCSVResource() throws Exception {
        String line = readTestData();
        assertNotNull("file must contain config line", line);
        final String[] split = line.split(" ");
        assertTrue(testName + " require 1 param", split.length >= 1);
        // first line starts with csv data file name
        CSVFormat format = CSVFormat.newFormat(',').withQuoteChar('"');
        boolean checkComments = false;
        for (int i = 1; i < split.length; i++) {
            final String option = split[i];
            final String[] option_parts = option.split("=", 2);
            if ("IgnoreEmpty".equalsIgnoreCase(option_parts[0])) {
                format = format.withIgnoreEmptyLines(Boolean.parseBoolean(option_parts[1]));
            } else if ("IgnoreSpaces".equalsIgnoreCase(option_parts[0])) {
                format = format.withIgnoreSurroundingSpaces(Boolean.parseBoolean(option_parts[1]));
            } else if ("CommentStart".equalsIgnoreCase(option_parts[0])) {
                format = format.withCommentStart(option_parts[1].charAt(0));
            } else if ("CheckComments".equalsIgnoreCase(option_parts[0])) {
                checkComments = true;
            } else {
                fail(testName + " unexpected option: " + option);
            }
        }
        line = readTestData(); // get string version of format
        assertEquals(testName + " Expected format ", line, format.toString());

        // Now parse the file and compare against the expected results
        CSVParser parser = CSVParser.parseResource("CSVFileParser/" + split[0], Charset.forName("UTF-8"),
                this.getClass().getClassLoader(), format);
        for (final CSVRecord record : parser) {
            String parsed = record.toString();
            if (checkComments) {
                final String comment = record.getComment().replace("\n", "\\n");
                if (comment != null) {
                    parsed += "#" + comment;
                }
            }
            final int count = record.size();
            assertEquals(testName, readTestData(), count + ":" + parsed);
        }
    }
}
