package com.aviva.controlfarmacia.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.aviva.controlfarmacia.R
import com.aviva.controlfarmacia.domain.model.Dosage
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun Dosage.label(): String =
    stringResource(R.string.dosage_format, value, stringResource(unit.labelRes()))

@Composable
fun YearMonth.label(): String = stringResource(R.string.expiry_date_format, monthValue, year)

private val SHORT_TIME: DateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

fun LocalTime.toDisplayString(): String = format(SHORT_TIME)
