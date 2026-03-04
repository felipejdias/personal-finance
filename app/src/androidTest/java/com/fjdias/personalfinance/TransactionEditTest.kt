package com.fjdias.personalfinance

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Testes referentes ao Cenário 2 de TEST_SCENARIOS.md
 * Valida a edição completa de uma transação.
 */
@RunWith(AndroidJUnit4::class)
class TransactionEditTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testEditTransactionTitle() {
        // 1. Garantir que estamos na aba de Transações
        composeTestRule.onNodeWithText("Transações").performClick()

        // 2. Clicar em uma transação existente (se houver) ou criar uma para testar
        // Para este teste ser independente, vamos primeiro criar uma transação rápida
        composeTestRule.onNodeWithContentDescription("Adicionar Transação").performClick()
        composeTestRule.onNodeWithText("Título").performTextReplacement("Gasto Original")
        composeTestRule.onNodeWithText("Valor (R$)").performTextReplacement("50.0")
        composeTestRule.onNodeWithText("Salvar").performClick()

        // 3. Clicar no item recém criado para editar (Contexto: Cenário 2)
        composeTestRule.onNodeWithText("Gasto Original").performClick()

        // 4. Alterar o título (Ação: Cenário 2)
        composeTestRule.onNodeWithText("Título").performTextReplacement("Gasto Editado")
        composeTestRule.onNodeWithText("Salvar").performClick()

        // 5. Verificar resultado (Resultado Esperado: Cenário 2)
        composeTestRule.onNodeWithText("Gasto Editado").assertIsDisplayed()
        composeTestRule.onNodeWithText("Gasto Original").assertDoesNotExist()
    }
}
