package com.fjdias.personalfinance

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RendaIntegrityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testRendaCategoryIsLocked() {
        // 1. Ir para a tela de Categorias
        composeTestRule.onNodeWithText("Categorias").performClick()

        // 2. Verificar se a categoria Renda existe na lista usando a tag
        composeTestRule.onNodeWithTag("CategoryNameText_Renda").assertIsDisplayed()

        // 3. Verificar se o ícone de cadeado (Locked) aparece para a Renda
        composeTestRule.onNodeWithTag("LockIcon_Renda").assertIsDisplayed()
        
        // 4. Garantir que NÃO existem botões de Editar ou Excluir para a Renda
        composeTestRule.onNodeWithTag("EditCategoryButton_Renda").assertDoesNotExist()
        composeTestRule.onNodeWithTag("DeleteCategoryButton_Renda").assertDoesNotExist()
    }
}
