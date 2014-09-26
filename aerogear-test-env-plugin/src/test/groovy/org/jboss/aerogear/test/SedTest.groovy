package org.jboss.aerogear.test

import static org.junit.Assert.*

import org.arquillian.spacelift.execution.Tasks
import org.arquillian.spacelift.execution.impl.DefaultExecutionServiceFactory
import org.jboss.aerogear.test.utils.SedTool
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

class SedTest {

    private File tempFile

    private def contentBefore = '''
this is some testing (temporary) file where I want to 
replace strings for another ones by Spacelift Sed tool
This is the third row in it.
'''

    private def contentAfter1 = '''
this is sOme testing (tempOrary) file where I want tO 
replace strings fOr anOther Ones by Spacelift Sed tOOl
This is the third rOw in it.
'''
    private def contentAfter2 = '''
.... .. .... ....... ........... .... ..... . .... .. 
....... ....... ... ....... .... .. ......... ... ....
.... .. ... ..... ... .. ...
'''

    private def contentAfter3 = '''
.his is some testing (temporary) file where I want to 
.eplace strings for another ones by Spacelift Sed tool
.his is the third row in it.
'''

    @BeforeClass
    public static void setup() {
        Tasks.setDefaultExecutionServiceFactory(new DefaultExecutionServiceFactory());
    }

    @Before
    void before() {
        tempFile = File.createTempFile("sed-test", ".tmp");
    }

    @After
    void after() {
        if (!tempFile.delete()) {
            throw new IllegalStateException("Unable to delete file: " + tempFile.getAbsoluteFile())
        }
    }

    @Test
    public void replacementTest() {

        tempFile.write(contentBefore)

        Tasks.prepare(SedTool).file(tempFile)
                .replace("o")
                .replaceWith("O")
                .execute().await()

        assertEquals(contentAfter1, tempFile.text)
    }

    @Test
    public void replacementRegexTest() {

        tempFile.write(contentBefore)

        Tasks.prepare(SedTool).file(tempFile)
                .replace("[^ ]")
                .replaceWith(".")
                .execute().await()

        assertEquals(contentAfter2, tempFile.text)
    }

    @Test
    public void firstOccurrenceRegexTest() {

        tempFile.write(contentBefore)

        Tasks.prepare(SedTool).file(tempFile)
                .firstOccurrence()
                .replace("[^ ]")
                .replaceWith(".")
                .execute().await()

        assertEquals(contentAfter3, tempFile.text)
    }
}
