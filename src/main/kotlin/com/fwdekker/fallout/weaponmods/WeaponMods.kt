package com.fwdekker.fallout.weaponmods

import mu.KLogging
import java.io.File
import kotlin.system.exitProcess


data class WeaponMod(
    val esm: ESM,
    val formIDTemplate: String,
    val weapon: Weapon,
    val description: String,
    val components: Map<Component, Int>,
    val perkRequirements: Map<Perk, Int>,
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
        ): WeaponMod {
            require(looseMod.file == objectModifier.file && objectModifier.file == craftableObject.file) { "?" }

            val esm = ESM.get(looseMod.file)
            require(esm != null) { "Could not find ESM `${looseMod.file}`." }

            val model = Model.get(looseMod.model)
            require(model != null) { "Could not find model `${looseMod.model}`." }

            val perks = craftableObject.conditions
                .filter { it.perk.isNotEmpty() }
                .map {
                    // TODO handle null
                    Pair(Perk.get(it.perk.substringBefore("0"))!!, it.perk.substringAfter("0").toInt())
                }
                .toMap()

            val weapon = Weapon.get(objectModifier.weaponName)
            require(weapon != null) { "Could not find weapon `${objectModifier.weaponName}`." }

            val formIDTemplate = formIDtoTemplate(looseMod.formID.toString(16))
            val components = craftableObject.components  // TODO fix component capitalisation and links
                .map { Pair(database.components.single { c -> c.editorID == it.component }, it.count) }
                .toMap()

            return WeaponMod(
                esm = esm!!,
                formIDTemplate = formIDTemplate,
                weapon = weapon!!,
                description = objectModifier.description,
                components = components,
                perkRequirements = perks,
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

    private val appearanceString = // TODO clean up
        if (games.size == 1 && games[0] == ESM.get("Fallout4.esm"))
            "''${games[0].link}''"
        else if (games.size == 2 && games.contains(ESM.get("Fallout4.esm")))
            "''${ESM.get("Fallout4.esm")!!.link}'' and its [[Fallout 4 add-ons|add-on]] ''${games.asSequence()
                .filterNot { it == ESM.get("Fallout4.esm") }
                .first()
                .link
            }''"
        else
            "''${ESM.get("Fallout4.esm")!!.link}'' and its [[Fallout 4 add-ons|add-ons]] ${games.dropLast(1)
                .asSequence()
                .filterNot { it == ESM.get("Fallout4.esm") }
                .map { it.link }
                .joinToString(", ") { "''$it''" }
            } and ''${games.last().link}''"


    private fun createInfobox(): WikiTemplate {
        return WikiTemplate(
            "Infobox item",
            listOf(
                "games" to games.joinToString(", ") { it.abbreviation },
                "type" to "mod",
                "icon" to "",
                "image" to image,
                "effects" to "<!-- Variable -->", // TODO
                "modifies" to weaponMods.joinToString("<br />") { it.weapon.link.toString(capitalize = true) },
                "value" to namedAggregation { it.value.toString() },
                "weight" to namedAggregation { it.weight.toString() },
                "baseid" to namedAggregation { it.formIDTemplate }
            )
        )
    }

    private fun createEffects(): Section {
        return Section("Effects", "<!-- Variable --> // TODO")
    }

    private fun createProductionTable(mod: WeaponMod): String {
        return CraftingTable(
            materials = mod.components.map { it.key.name to it.value },
            workspace = "[[Weapons workbench]]",
            perks = mod.perkRequirements.map { Pair(it.key.name, it.value) }, // TODO insert link to perk
            products = listOf(modName.capitalize() to 1)
        ).toString()
    }

    private fun createProduction(): Section {
        return if (weaponMods.size == 1)
            Section("Production", createProductionTable(weaponMods[0]))
        else
            Section("Production",
                "",
                subsections = weaponMods.map { weaponMod ->
                    Section(
                        weaponMod.weapon.link.toString(capitalize = true),
                        createProductionTable(weaponMod),
                        level = 3
                    )
                })
    }

    private fun createLocation(): Section {
        return Section("Location", "The $modName can be crafted at any [[weapons workbench]].")
    }


    fun createPage(): Article {
        return Article().also { page ->
            page.games += games.map { it.abbreviation }
            page.infoboxes += createInfobox()
            page.intro = "The '''$modName''' is a [[Fallout 4 weapon mods|weapon mod]] in $appearanceString."
            page.sections +=
                listOf(
                    createEffects(),
                    createProduction(),
                    createLocation()
                )
            page.navboxes += WikiTemplate("Navbox weapon mods FO4")
            page.categories += games.mapNotNull { it.modCategory }.map { Category(it) }
        }
    }


    private fun namedAggregation(property: (WeaponMod) -> String): String {
        // TODO remove this function?
        return if (weaponMods.map(property).distinct().size == 1)
            property(weaponMods[0]) // TODO check if empty
        else
            weaponMods.joinToString("<br />") { "${property(it)} (${it.weapon.name})" }
    }
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
    println(selection.createPage().toString())
}
