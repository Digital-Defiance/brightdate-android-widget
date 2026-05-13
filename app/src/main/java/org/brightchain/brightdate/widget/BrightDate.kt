package org.brightchain.brightdate.widget

/**
 * Minimal BrightDate computation.
 *
 * BrightDate = (taiUnixSeconds − J2000_TAI_UNIX_S) / 86400
 * where J2000_TAI_UNIX_S = 946_727_967.816 (TAI seconds since Unix epoch at J2000.0).
 *
 * Android's System.currentTimeMillis() is UTC-based, so we add the current
 * TAI−UTC offset (37 seconds, unchanged since 2017-01-01) before subtracting.
 *
 * For a phone home-screen widget this is more than accurate enough; the next
 * IERS leap-second announcement would require bumping [TAI_MINUS_UTC_SECONDS].
 */
object BrightDate {

    /** TAI seconds since Unix epoch at J2000.0 (see README). */
    private const val J2000_TAI_UNIX_S = 946_727_967.816

    private const val SECONDS_PER_DAY = 86_400.0

    /** BrightDate value (decimal days since J2000.0) for the given Unix ms. */
    fun fromUnixMs(unixMs: Long): Double {
        val utcSeconds = unixMs / 1000.0
        val offset = LeapSeconds.taiMinusUtcFromMs(unixMs)
        val taiUnixSeconds = utcSeconds + offset
        return (taiUnixSeconds - J2000_TAI_UNIX_S) / SECONDS_PER_DAY
    }

    /** BrightDate value for "now". */
    fun now(): Double = fromUnixMs(System.currentTimeMillis())

    /**
     * Format a BrightDate value as `DDDDD.ddddd` with the given fractional digits.
     * Uses Locale.ROOT so the decimal separator is always '.'.
     */
    fun format(value: Double, fractionDigits: Int = 5): String =
        String.format(java.util.Locale.ROOT, "% .${fractionDigits}f", value)
}
