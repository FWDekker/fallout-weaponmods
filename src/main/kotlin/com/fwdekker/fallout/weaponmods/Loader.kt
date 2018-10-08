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

class WeaponSelection(private val modName: String, private val weaponMods: List<WeaponMod>) {
    private val image = weaponMods
        .groupingBy { it.image }
        .eachCount().entries
        .maxBy { it.value }?.key
        ?: "" // TODO log empty image
    private val games = weaponMods
        .map { it.esm }
        .distinct()
        .toList()
        .sortedBy { it.name }
    private val ingredients =
        weaponMods.flatMap { mod -> mod.components.map { it.first } }
            .asSequence()
            .distinct()
            .sortedBy { it.name }
            .toList()

    private val appearanceString =
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

    private val longestWeaponLink = weaponMods.asSequence()
        .map { it.weapon.getWikiLink() }
        .maxBy { it.length }!!
        .length
    private val longestIngredientNumber = ingredients
        .map { ing ->
            Pair(
                ing,
                weaponMods
                    .flatMap { it.components }
                    .asSequence()
                    .filter { it.first == ing }
                    .maxBy { it.second }!!
                    .second.toString().length
            )
        }
        .toMap()

    private fun createInfobox() =
        """
        {{Infobox item
        |games        =${games.joinToString(", ") { it.abbreviation }}
        |type         =mod
        |icon         =
        |image        =$image
        |effects      =<!-- Variable --> // TODO
        |modifies     =${weaponMods.joinToString("<br />") { it.weapon.getWikiLink() }}
        |value        =${namedAggregation { it.value.toString() }}
        |weight       =${namedAggregation { it.weight.toString() }}
        |baseid       =${namedAggregation { it.formIDTemplate }}
        }}{{Games|${games.joinToString("|") { it.abbreviation }}}}
        """.trimIndent()

    private fun createHeading() =
        """
        The '''$modName''' is a [[Fallout 4 weapon mods|weapon mod]] in ${appearanceString}.
        """.trimIndent()

    private fun createEffects() =
        """
        ==Effects==
        <!-- Variable --> // TODO
        """.trimIndent()

    private fun singleTable() =
        """
        {{Crafting table
        ${weaponMods[0].components.asSequence()
            .sortedBy { it.first.name }
            .mapIndexed { index, pair ->
                """
                |${"material$index".padEnd(9 + ingredients.size)} =${pair.first.name}
                |${"material#$index".padEnd(9 + ingredients.size)} =${pair.second}
                """.trimIndent()
            }
        }
        |${"workspace".padEnd(9 + ingredients.size)} =[[Weapons workbench]]
        |${"product1".padEnd(9 + ingredients.size)} =$modName
        |${"product#1".padEnd(9 + ingredients.size)} =1
        }}
        """.trimIndent()

    private fun multiTable() =
        """
        {|class="va-table va-table-center sortable"
        !style="width:180px;"| Weapon
        ${ingredients.joinToString("\n") { "!style=\"width:180px;\"| ${it.name}" }}
        ${weaponMods
            .joinToString("") { mod ->
                "|-\n| ${mod.weapon.getWikiLink().padEnd(longestWeaponLink)} ${ingredients.asSequence()
                    .map { ing -> Pair(ing, mod.components.singleOrNull { comp -> ing == comp.first }?.second ?: 0) }
                    .joinToString("") { "|| ${it.second.toString().padStart(longestIngredientNumber[it.first]!!)} " }}\n"
            }
        }
        |}
        """.trimIndent()

    private fun createProduction() =
        "==Production==" +
            if (weaponMods.size == 1)
                singleTable()
            else
                multiTable()

    private fun createLocations() =
        "==Locations==\nThe $modName can be crafted at any [[weapons workbench]]."

    private fun createCategories() =
        games.asSequence()
            .mapNotNull { it.modCategory }
            .joinToString("\n") { "[[Category:$it]]" }

    val wikiPageString =
        """
        ${createInfobox()}

        ${createHeading()}

        ${createEffects()}

        ${createProduction()}

        ${createLocations()}

        {{Navbox weapon mods FO4}}

        ${createCategories()}
        """.trimIndent()

    fun namedAggregation(property: (WeaponMod) -> String) =
        if (weaponMods.map(property).distinct().size == 1)
            property(weaponMods[0]) // TODO check if empty
        else
            weaponMods.joinToString("<br />") { "${property(it)} (${it.weapon.name})" }
}

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
    val looseMods = database.looseMods
        .filter { it.name.toLowerCase().contains(modName.toLowerCase()) }
        .toList()
    val weaponMods = looseMods
        .mapNotNull { WeaponMod.create(it, database) }
        .sortedBy { it.weapon.name }
        .toList()
    if (weaponMods.isEmpty()) {
        println("No weapon mods found")
        return
    }

    val selection = WeaponSelection(modName, weaponMods)

    println(selection.wikiPageString)
}
