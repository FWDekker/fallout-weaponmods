package com.fwdekker.fallout.weaponmods

import com.beust.klaxon.Klaxon
import mu.KLogging
import java.io.File


data class GameDatabase(
    val looseMods: List<LooseMod>,
    val objectModifiers: List<ObjectModifier>,
    val craftableObjects: List<CraftableObject>,
    val components: List<Component>,
    val weapons: List<XWeapon>
) {
    companion object : KLogging() {
        fun fromDirectory(directory: File): GameDatabase? {
            val looseMods = parseFile<LooseMod>(File(directory, "misc.json"))
                ?: return null
            val objectModifiers = parseFile<ObjectModifier>(File(directory, "omod.json"))
                ?: return null
            val craftableObjects = parseFile<CraftableObject>(File(directory, "cobj.json"))
                ?: return null
            val components = parseFile<Component>(File(directory, "cmpo.json"))
                ?: return null
            val weapons = parseFile<XWeapon>(File(directory, "weap.json"))
                ?: return null

            return GameDatabase(looseMods, objectModifiers, craftableObjects, components, weapons)
        }

        private inline fun <reified T> parseFile(file: File) = Klaxon().parseArray<T>(file.inputStream())
    }
}
