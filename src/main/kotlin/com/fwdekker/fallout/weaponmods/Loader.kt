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
    val esm: ESM,
    val formIDTemplate: String,
    val weapon: Weapon,
    val effects: String,
    val components: List<Pair<Component, Int>>, // TODO fix component capitalisation
    val value: Int,
    val weight: Double,
    val image: String
) {
    constructor(
        looseMod: LooseMod,
        objectModifier: ObjectModifier,
        craftableObject: CraftableObject,
        database: GameDatabase
    ) :
        this(
            esm = ESM.get(looseMod.file)!!, // TODO handle errors
            formIDTemplate = looseMod.formID.toString(16).let {
                if (it.length > 6) "{{DLC ID|${it.take(6)}}}"
                else "{{ID|$it}}"
            },
            weapon = Weapon.get(objectModifier.weaponName.toLowerCase())
                ?: Weapon("", "", "", ""), // TODO handle errors
            effects = objectModifier.description,
            components = craftableObject.components
                .map { Pair(database.components.single { c -> c.editorID == it.component }, it.count) },
            value = looseMod.value,
            weight = looseMod.weight,
            image = Model.get(looseMod.model)!!.image // TODO handle errors
        ) {
        require(looseMod.file == objectModifier.file && objectModifier.file == craftableObject.file) { "?" }
        // TODO improve message
    }

    companion object {
        fun create(looseMod: LooseMod, database: GameDatabase): WeaponMod? {
            val objectModifier = database.objectModifiers.singleOrNull { it.looseMod == looseMod.editorID }
            if (objectModifier == null) {
                println("Could not create weapon mod object") // TODO use logger
                return null
            }

            val craftableObject = database.craftableObjects.singleOrNull { it.createdMod == objectModifier.editorID }
            if (craftableObject == null) {
                println("Could not create weapon mod object")
                return null
            }

            return WeaponMod(looseMod, objectModifier, craftableObject, database)
        }
    }
}

class WeaponSelection(private val modName: String, database: GameDatabase) {
    val looseMods = database.looseMods
        .filter { it.name.toLowerCase().contains(modName.toLowerCase()) }
        .toList()
    val weaponMods = looseMods
        .mapNotNull { WeaponMod.create(it, database) }
        .sortedBy { it.weapon.name }
        .toList()
    val image = weaponMods
        .groupingBy { it.image }
        .eachCount().entries
        .maxBy { it.value }?.key
        ?: "" // TODO log empty image
    val games = weaponMods
        .map { it.esm }
        .distinct()
        .toList()
        .sortedBy { it.name }
    val appearanceString =
        if (games.size == 1 && games[0] == ESM.get("Fallout4.esm"))
            games[0].getWikiLink()
        else if (games.size == 2 && games.contains(ESM.get("Fallout4.esm")))
            "${ESM.get("Fallout4.esm")!!.getWikiLink()} and its [[Fallout 4 add-ons|add-on]] ${games.asSequence()
                .filterNot { it == ESM.get("Fallout4.esm") }
                .first()
                .getWikiLink()
            }"
        else
            "${ESM.get("Fallout4.esm")!!.getWikiLink()} and its [[Fallout 4 add-ons|add-ons]] ${games.dropLast(1)
                .asSequence()
                .filterNot { it == ESM.get("Fallout4.esm") }
                .map { it.getWikiLink() }
                .joinToString(", ")
            } and ${games.last().getWikiLink()}"
}

fun namedAggregation(weaponMods: List<WeaponMod>, property: (WeaponMod) -> String) =
    if (weaponMods.map(property).distinct().size == 1)
        property(weaponMods[0]) // TODO check if empty
    else
        weaponMods.joinToString("<br />") { "${property(it)} (${it.weapon.name})" }

fun main(args: Array<String>) {
    print("Enter JSON location: ")
    val databaseLocation = readLine() ?: exitProcess(-1)
    val database = GameDatabase(File(databaseLocation))

    while (true) {
        print("Enter weapon mod name: ")
        val targetName = readLine() ?: exitProcess(-1)
        launch(database, targetName)
    }
}

private fun launch(database: GameDatabase, modName: String) {
    val selection = WeaponSelection(modName, database)
    if (selection.weaponMods.isEmpty()) {
        println("No weapon mods found")
        return
    }

    val games = selection.games
    val ingredients =
        selection.weaponMods.flatMap { mod -> mod.components.map { it.first } }
            .asSequence()
            .distinct()
            .sortedBy { it.name }
            .toList()
    val longestWeaponLink = selection.weaponMods.asSequence()
        .map { it.weapon.getWikiLink() }
        .maxBy { it.length }!!
        .length
    val longestIngredientNumber = ingredients
        .map { ing ->
            Pair(
                ing,
                selection.weaponMods
                    .flatMap { it.components }
                    .asSequence()
                    .filter { it.first == ing }
                    .maxBy { it.second }!!
                    .second.toString().length
            )
        }
        .toMap()

    println("""
{{Infobox item
|games        =${games.joinToString(", ") { it.abbreviation }}
|type         =mod
|icon         =
|image        =${selection.image}
|effects      =<!-- Variable --> // TODO
|modifies     =${selection.weaponMods.joinToString("<br />") { it.weapon.getWikiLink() }}
|value        =${namedAggregation(selection.weaponMods) { it.value.toString() }}
|weight       =${namedAggregation(selection.weaponMods) { it.weight.toString() }}
|baseid       =${namedAggregation(selection.weaponMods) { it.formIDTemplate }}
}}{{Games|${games.joinToString("|") { it.abbreviation }}}}

The '''$modName''' is a [[Fallout 4 weapon mods|weapon mod]] in ${selection.appearanceString}.

==Effects==
<!-- Variable --> // TODO

==Production==
${
    if (selection.weaponMods.size == 1)
        """
{{Crafting table
${selection.weaponMods[0].components.asSequence().sortedBy { it.first.name }.mapIndexed { index, pair ->
            """
|${"material$index".padEnd(9 + ingredients.size)} =${pair.first.name}
|${"material#$index".padEnd(9 + ingredients.size)} =${pair.second}
""".trimIndent()
        }}
|${"workspace".padEnd(9 + ingredients.size)} =[[Weapons workbench]]
|${"product1".padEnd(9 + ingredients.size)} =$modName
|${"product#1".padEnd(9 + ingredients.size)} =1
}}
        """.trimIndent()
    else
        """
{|class="va-table va-table-center sortable"
!style="width:180px;"| Weapon
${ingredients.joinToString("\n") { "!style=\"width:180px;\"| ${it.name}" }}
${selection.weaponMods.joinToString("") { mod ->
            "|-\n| ${mod.weapon.getWikiLink().padEnd(longestWeaponLink)} ${ingredients.asSequence()
                .map { ing -> Pair(ing, mod.components.singleOrNull { comp -> ing == comp.first }?.second ?: 0) }
                .joinToString("") { "|| ${it.second.toString().padStart(longestIngredientNumber[it.first]!!)} " }}\n"
        }}
|}
""".trimIndent()
    }

==Locations==
The $modName can be crafted at any [[weapons workbench]].

{{Navbox weapon mods FO4}}

${games.asSequence()
        .mapNotNull { it.modCategory }
        .joinToString("\n") { "[[Category:$it]]" }}
    """.trimIndent())
}
