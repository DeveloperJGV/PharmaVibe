package com.aviva.controlfarmacia.domain.scan

import java.time.YearMonth

/**
 * Finds the expiration month/year in the text printed on a medication box.
 *
 * Dates next to an expiry keyword ("EXP", "CAD", "VENCE", "best before"...) win; otherwise the
 * latest plausible date is used, because manufacturing and lot dates are always earlier than
 * the expiry. Returns `null` when nothing looks like a date.
 */
object ExpiryDateParser {

    private val KEYWORD = Regex("""\b(exp|cad|venc|caduc|best before|use by|bbe|fecha)""", RegexOption.IGNORE_CASE)

    // "03/2028", "03-2028", "03.2028", "3 / 2028"
    private val MONTH_YEAR4 = Regex("""(?<!\d)(\d{1,2})\s*[/\-.]\s*(20\d{2})(?!\d)""")

    // "2028-03", "2028/03/15"
    private val YEAR4_MONTH = Regex("""(?<!\d)(20\d{2})\s*[/\-.]\s*(\d{1,2})(?!\d)""")

    // "03/28" (only trusted next to a keyword, it is too ambiguous otherwise)
    private val MONTH_YEAR2 = Regex("""(?<!\d)(\d{1,2})\s*[/\-.]\s*(\d{2})(?!\d)""")

    // "MAR 2028", "mar/28", "Marzo 2028", "DEC28"
    private val MONTH_NAME = Regex(
        """\b(ene|jan|feb|mar|abr|apr|may|jun|jul|ago|aug|sep|set|oct|nov|dic|dec)[a-z]*\.?\s*[/\-]?\s*(20\d{2}|\d{2})(?!\d)""",
        RegexOption.IGNORE_CASE
    )

    private val MONTH_BY_NAME = mapOf(
        "ene" to 1, "jan" to 1, "feb" to 2, "mar" to 3, "abr" to 4, "apr" to 4, "may" to 5,
        "jun" to 6, "jul" to 7, "ago" to 8, "aug" to 8, "sep" to 9, "set" to 9, "oct" to 10,
        "nov" to 11, "dic" to 12, "dec" to 12
    )

    private const val YEARS_BACK = 5L
    private const val YEARS_AHEAD = 15L

    fun parse(text: String, today: YearMonth): YearMonth? {
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) return null

        val keywordLines = lines.indices
            .filter { KEYWORD.containsMatchIn(lines[it]) }
            .flatMap { listOf(it, it + 1) }
            .filter { it < lines.size }
            .toSet()

        val plausible = { ym: YearMonth ->
            !ym.isBefore(today.minusYears(YEARS_BACK)) && !ym.isAfter(today.plusYears(YEARS_AHEAD))
        }

        val byKeyword = keywordLines.flatMap { candidates(lines[it], trustTwoDigitYear = true) }.filter(plausible)
        if (byKeyword.isNotEmpty()) return byKeyword.max()

        return lines.flatMap { candidates(it, trustTwoDigitYear = false) }.filter(plausible).maxOrNull()
    }

    private fun candidates(line: String, trustTwoDigitYear: Boolean): List<YearMonth> {
        val found = mutableListOf<YearMonth>()
        MONTH_YEAR4.findAll(line).forEach { m ->
            yearMonthOrNull(m.groupValues[2].toInt(), m.groupValues[1].toInt())?.let(found::add)
        }
        YEAR4_MONTH.findAll(line).forEach { m ->
            yearMonthOrNull(m.groupValues[1].toInt(), m.groupValues[2].toInt())?.let(found::add)
        }
        MONTH_NAME.findAll(line).forEach { m ->
            val month = MONTH_BY_NAME[m.groupValues[1].lowercase()] ?: return@forEach
            yearMonthOrNull(expandYear(m.groupValues[2]), month)?.let(found::add)
        }
        if (trustTwoDigitYear && found.isEmpty()) {
            MONTH_YEAR2.findAll(line).forEach { m ->
                yearMonthOrNull(expandYear(m.groupValues[2]), m.groupValues[1].toInt())?.let(found::add)
            }
        }
        return found
    }

    private fun expandYear(raw: String): Int = raw.toInt().let { if (it < 100) 2000 + it else it }

    private fun yearMonthOrNull(year: Int, month: Int): YearMonth? =
        if (month in 1..12) YearMonth.of(year, month) else null
}
