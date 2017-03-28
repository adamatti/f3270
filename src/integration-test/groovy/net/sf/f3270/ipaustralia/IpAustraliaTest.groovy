package net.sf.f3270.ipaustralia

import net.sf.f3270.FieldIdentifier
import net.sf.f3270.IntegrationTestBase
import org.junit.Assert
import org.junit.Test

class IpAustraliaTest extends IntegrationTestBase {

    String getHostname() {
        "pericles.ipaustralia.gov.au"
    }

    Mode getMode() {
        Mode.REPLAY
    }

    @Test
    void testIpAustralia() {
        connect()

        assertText(terminal, "A U S T R A L I A")
        terminal.enter()
        assertText(terminal, "DISCLAIMER")
        terminal.enter()
        assertText(terminal, "Logon in progress...")
        sleep(100)
        terminal.enter()
        Assert.assertEquals(Boolean.TRUE, (Boolean)terminal.screenHasLabel(new FieldIdentifier("command")))
        Assert.assertEquals(Boolean.FALSE, (Boolean)terminal.screenHasLabel(new FieldIdentifier("rubbish_label")))
        terminal.write(new FieldIdentifier("command"), "1")
        terminal.read(new FieldIdentifier("command"))
        terminal.enter()
        terminal.enter()
        terminal.write(new FieldIdentifier("command"), "2")
        terminal.enter()
        terminal.write(new FieldIdentifier("trade mark number"), "123")

        disconnect()
    }



}
