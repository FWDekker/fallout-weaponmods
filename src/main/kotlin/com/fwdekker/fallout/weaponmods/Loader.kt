package com.fwdekker.fallout.weaponmods

import com.beust.klaxon.Klaxon
import java.io.File
import kotlin.system.exitProcess


data class GameDatabase(
    val looseMods: List<LooseMod>,
    val objectModifiers: List<ObjectModifier>,
    val craftableObjects: List<CraftableObject>,
    val components: List<Component>
) {
    constructor(directory: File) : this(
        Klaxon().parseArray<LooseMod>(File(directory, "misc.json").inputStream())!!,
        Klaxon().parseArray<ObjectModifier>(File(directory, "omod.json").inputStream())!!,
        Klaxon().parseArray<CraftableObject>(File(directory, "cobj.json").inputStream())!!,
        Klaxon().parseArray<Component>(File(directory, "cmpo.json").inputStream())!!
    )
}

data class WeaponMod(
    val looseMod: LooseMod,
    val objectModifier: ObjectModifier,
    val craftableObject: CraftableObject
) {
    companion object {
        fun create(looseMod: LooseMod, database: GameDatabase): WeaponMod {
            val objectModifier = database.objectModifiers.single { it.looseMod == looseMod.editorID }
            val craftableObject = database.craftableObjects.single { it.createdMod == objectModifier.editorID }

            return WeaponMod(looseMod, objectModifier, craftableObject)
        }
    }
}


fun main(args: Array<String>) {
    print("Enter JSON location: ")
    val databaseLocation = readLine() ?: exitProcess(-1)
    val database = GameDatabase(File(databaseLocation))

    print("Enter weapon mod name: ")
    val targetName = readLine() ?: exitProcess(-1)

    launch(database, targetName)
}

private fun launch(database: GameDatabase, modName: String) {
    val looseMods = database.looseMods.asSequence().filter { it.name.toLowerCase().contains(modName) }.toList()
    val weaponMods = looseMods.map { WeaponMod.create(it, database) }

    println("""
        {{Infobox item
        |games        =FO4
        |type         =mod
        |icon         =<!-- Variable? -->
        |image        =Fo4 item Mod type <!-- Variable -->.png
        |effects      =<!-- Variable -->
        |modifies     =<!-- Variable -->
        |value        =<!-- Variable -->
        |weight       =<!-- Variable -->
        |baseid       =<!-- Variable -->
        }}{{Games|FO4}}

        The '''$modName''' is a [[Fallout 4 weapon mods|weapon mod]] in ''[[Fallout 4]]'' <!-- Variable -->.

        ==Effects==
        <!-- Variable -->

        ==Production==
        <!-- Variable -->

        ==Locations==
        The $modName can be crafted at any [[weapons workbench]].

        {{Navbox weapon mods FO4}}

        [[Category:Fallout 4 weapon mods]]<!-- Variable -->
    """.trimIndent())
}
