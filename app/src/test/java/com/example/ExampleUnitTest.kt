package com.example

import com.example.generator.CodeValidator
import com.example.generator.WebsiteGeneratorEngine
import com.example.generator.WebsiteTestRunner
import com.example.model.Project
import com.example.model.ProjectCategory
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testBioscienceCategoryDetection() {
        val plan = WebsiteGeneratorEngine.analyzeRequirements("Create a genetics lab portal with DNA sequence analyzer and dilution tool")
        assertEquals("BioGenesis Science Portal", plan.projectName)
        assertTrue(plan.coreFeatures.isNotEmpty())
        assertTrue(plan.scienceFeatures.isNotEmpty())
    }

    @Test
    fun testEcommerceCategoryDetection() {
        val plan = WebsiteGeneratorEngine.analyzeRequirements("Create an online store for modern footwear and clothing with shopping cart")
        assertTrue(plan.coreFeatures.any { it.contains("cart", ignoreCase = true) })
    }

    @Test
    fun testUrduPromptSupport() {
        val plan = WebsiteGeneratorEngine.analyzeRequirements("ایک آن لائن سٹور بنائیں")
        assertTrue(plan.summary.isNotEmpty())
    }

    @Test
    fun testBioscienceFileGenerationAndValidation() {
        val project = Project(
            id = "test-bio-1",
            title = "BioGenesis Portal",
            description = "Test BioScience Portal",
            category = ProjectCategory.BIOSCIENCE
        )

        val files = WebsiteGeneratorEngine.generateProjectFiles(project, "BioScience portal")
        assertEquals(6, files.size)

        val htmlFile = files.find { it.path == "index.html" }
        assertNotNull(htmlFile)
        assertTrue(htmlFile!!.content.contains("<!DOCTYPE html>"))
        assertTrue(htmlFile.content.contains("DNA / RNA Sequence Workbench"))
        assertTrue(htmlFile.content.contains("C₁ × V₁ = C₂ × V₂"))

        val validationSteps = CodeValidator.validateProject(files)
        assertEquals(12, validationSteps.size)
        assertTrue("All 12 validation steps should pass", validationSteps.all { it.passed })

        val automatedTests = WebsiteTestRunner.runAutomatedTests(files)
        assertTrue(automatedTests.isNotEmpty())
        assertTrue("All automated tests should pass", automatedTests.all { it.passed })
    }
}
