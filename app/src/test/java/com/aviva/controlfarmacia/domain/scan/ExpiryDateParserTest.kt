package com.aviva.controlfarmacia.domain.scan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.YearMonth

class ExpiryDateParserTest {

    private val today = YearMonth.of(2026, 10)

    private fun parse(text: String) = ExpiryDateParser.parse(text, today)

    @Test
    fun `reads month slash year after an expiry keyword`() {
        assertEquals(YearMonth.of(2028, 3), parse("PARACETAMOL 500 mg\nLOTE A123\nCAD 03/2028"))
        assertEquals(YearMonth.of(2027, 11), parse("EXP: 11-2027"))
        assertEquals(YearMonth.of(2027, 5), parse("Vence 05.2027"))
    }

    @Test
    fun `reads year first and full dates`() {
        assertEquals(YearMonth.of(2028, 3), parse("EXP 2028-03"))
        assertEquals(YearMonth.of(2028, 3), parse("Best before 2028/03/15"))
        assertEquals(YearMonth.of(2028, 3), parse("CAD 30/03/2028"))
    }

    @Test
    fun `reads month names in spanish and english`() {
        assertEquals(YearMonth.of(2028, 3), parse("CAD MAR 2028"))
        assertEquals(YearMonth.of(2028, 12), parse("EXP DEC28"))
        assertEquals(YearMonth.of(2027, 8), parse("Caduca: Agosto 2027"))
    }

    @Test
    fun `two digit year is only trusted next to a keyword`() {
        assertEquals(YearMonth.of(2028, 3), parse("CAD 03/28"))
        assertNull(parse("Ref 03/28"))
    }

    @Test
    fun `date on the line after the keyword counts`() {
        assertEquals(YearMonth.of(2028, 6), parse("Fecha de caducidad\n06/2028"))
    }

    @Test
    fun `without keyword the latest date wins over the manufacturing date`() {
        assertEquals(YearMonth.of(2028, 1), parse("FAB 01/2026\n01/2028"))
    }

    @Test
    fun `keyword line wins over a later date elsewhere`() {
        assertEquals(YearMonth.of(2027, 2), parse("EXP 02/2027\nPRICE LIST 2029"))
    }

    @Test
    fun `ignores implausible dates and plain numbers`() {
        assertNull(parse("500 mg 20 tabletas"))
        assertNull(parse("EXP 13/2028"))
        assertNull(parse("CAD 01/2001"))
        assertNull(parse("CAD 01/2099"))
    }

    @Test
    fun `empty text yields nothing`() {
        assertNull(parse(""))
        assertNull(parse("   \n  "))
    }
}
