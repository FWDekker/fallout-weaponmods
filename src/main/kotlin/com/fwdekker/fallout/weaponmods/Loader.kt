package com.fwdekker.fallout.weaponmods

import com.beust.klaxon.Klaxon
import mu.KLogging
import java.io.File
import kotlin.system.exitProcess


data class GameDatabase(
    val looseMods: List<LooseMod>,
    val objectModifiers: List<ObjectModifier>,
    val craftableObjects: List<CraftableObject>,
    val components: List<Component>
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

            return GameDatabase(looseMods, objectModifiers, craftableObjects, components)
        }

        private inline fun <reified T> parseFile(file: File) = Klaxon().parseArray<T>(file.inputStream())
    }
}


data class WeaponMod(
    val esm: ESM,
    val formIDTemplate: String,
    val weapon: Weapon,
    val effects: String,
    val components: List<Pair<Component, Int>>,
    val value: Int,
    val weight: Double,
    val image: String
) {
    companion object : KLogging() {
        fun fromLooseMod(looseMod: LooseMod, database: GameDatabase): WeaponMod? {
            val objectModifier = database.objectModifiers.singleOrNull { it.looseMod == looseMod.editorID }
            if (objectModifier == null) {
                logger.warn { "Could not create weapon mod with omod `${looseMod.editorID}`." }
                return null
            }

            val craftableObject = database.craftableObjects.singleOrNull { it.createdMod == objectModifier.editorID }
            if (craftableObject == null) {
                logger.warn { "Could not create weapon mod with cobj `${objectModifier.editorID}`." }
                return null
            }

            return fromObjects(looseMod, objectModifier, craftableObject, database)
        }

        private fun fromObjects(
            looseMod: LooseMod,
            objectModifier: ObjectModifier,
            craftableObject: CraftableObject,
            database: GameDatabase
        ): WeaponMod? {
            require(looseMod.file == objectModifier.file && objectModifier.file == craftableObject.file) { "?" }

            val esm = ESM.get(looseMod.file)
            require(esm != null) { "Could not find ESM `${looseMod.file}`." }

            val model = Model.get(looseMod.model)
            require(model != null) { "Could not find model `${looseMod.model}`." }

            val formIDTemplate = formIDtoTemplate(looseMod.formID.toString(16))
            val weapon = Weapon.get(objectModifier.weaponName.toLowerCase())
                ?: return null
            val components = craftableObject.components  // TODO fix component capitalisation
                .map { Pair(database.components.single { c -> c.editorID == it.component }, it.count) }

            return WeaponMod(
                esm = esm!!,
                formIDTemplate = formIDTemplate,
                weapon = weapon,
                effects = objectModifier.description,
                components = components,
                value = looseMod.value,
                weight = looseMod.weight,
                image = model!!.image
            )
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
    val logger = KLogging().logger

    print("Enter JSON location: ")
    val databaseLocation = readLine()
        ?: logger.error { "No location was entered." }.let { exitProcess(-1) }
    val database = GameDatabase.fromDirectory(File(databaseLocation))
        ?: logger.error { "Failed to read database." }.let { exitProcess(-1) }

    while (true) {
        print("Enter weapon mod name: ")
        val targetName = readLine()
            ?: logger.error { "No weapon mod was entered." }.let { exitProcess(-1) }

        launch(database, targetName)
    }
}

private fun launch(database: GameDatabase, modName: String) {
    val logger = KLogging().logger

    val looseMods = database.looseMods
        .filter { it.name.toLowerCase().contains(modName.toLowerCase()) }
        .toList()
    val weaponMods = looseMods
        .mapNotNull { WeaponMod.fromLooseMod(it, database) }
        .sortedBy { it.weapon.name }
        .toList()
    if (weaponMods.isEmpty()) {
        logger.warn { "No weapon mods by the name `$modName` were found." }
        return
    }

    val selection = WeaponSelection(modName, weaponMods)
    println(selection.wikiPageString)
}
