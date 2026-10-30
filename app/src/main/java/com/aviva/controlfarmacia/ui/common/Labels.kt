package com.aviva.controlfarmacia.ui.common

import androidx.annotation.StringRes
import com.aviva.controlfarmacia.R
import com.aviva.controlfarmacia.domain.model.DosageUnit
import com.aviva.controlfarmacia.domain.model.PatientCategory

@StringRes
fun DosageUnit.labelRes(): Int = when (this) {
    DosageUnit.MILLIGRAM -> R.string.unit_mg
    DosageUnit.MILLILITER -> R.string.unit_ml
    DosageUnit.INTERNATIONAL_UNIT -> R.string.unit_iu
    DosageUnit.GRAM -> R.string.unit_g
    DosageUnit.CAPSULE -> R.string.unit_capsules
    DosageUnit.TABLET -> R.string.unit_tablets
    DosageUnit.DROP -> R.string.unit_drops
    DosageUnit.PUFF -> R.string.unit_puffs
    DosageUnit.PIECE -> R.string.unit_pieces
}

@StringRes
fun PatientCategory.labelRes(): Int = when (this) {
    PatientCategory.HUMAN -> R.string.patient_category_human
    PatientCategory.PET -> R.string.patient_category_pet
}

@StringRes
fun PatientCategory.nameFieldLabelRes(): Int = when (this) {
    PatientCategory.HUMAN -> R.string.patient_name_label
    PatientCategory.PET -> R.string.pet_name_label
}
