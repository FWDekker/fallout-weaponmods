package com.fwdekker.fallout.weaponmods


data class Component(
    val file: String,
    val formID: Int,
    val editorID: String,
    val name: String
)

data class LooseMod(
    val file: String,
    val formID: Int,
    val editorID: String,
    val name: String,
    val value: Int,
    val weight: Double,
    val model: String
)

data class ObjectModifier(
    val file: String,
    val formID: Int,
    val editorID: String,
    val name: String,
    val description: String,
    val looseMod: String,
    val weaponName: String
)

data class CraftableObject(
    val file: String,
    val formID: Int,
    val editorID: String,
    val createdMod: String,
    val components: List<CraftableObject.Component>,
    val conditions: List<CraftableObject.Condition>
) {
    data class Component(
        val component: String,
        val count: Int
    )

    data class Condition(
        val function: String,
        val perk: String,
        val keyword: String,
        val type: String,
        val comparison: String
    )
}
