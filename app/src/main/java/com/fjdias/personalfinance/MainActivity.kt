package com.fjdias.personalfinance

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fjdias.personalfinance.ui.theme.PersonalFinanceTheme
import java.util.*

enum class FinanceScreen {
    Transactions, Summary, Breakdown, Categories
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            PersonalFinanceTheme {
                val viewModel: FinanceViewModel = viewModel()
                val allTransactions by viewModel.allTransactions.collectAsState(initial = emptyList())
                val allCategories by viewModel.allCategories.collectAsState(initial = emptyList())
                val context = LocalContext.current
                
                var searchQuery by remember { mutableStateOf("") }
                var selectedMonth by remember { mutableStateOf<Int?>(null) }
                var selectedCategoryName by remember { mutableStateOf<String?>(null) }
                var showDeleteDialog by remember { mutableStateOf(false) }
                var currentScreen by remember { mutableStateOf(FinanceScreen.Transactions) }

                val filePickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    uri?.let { viewModel.importCsv(context, it) }
                }

                val filteredTransactions = allTransactions.filter {
                    val matchesSearch = it.title.contains(searchQuery, ignoreCase = true)
                    val matchesMonth = selectedMonth == null || it.date.monthValue == selectedMonth
                    val matchesCategory = selectedCategoryName == null || it.categoryName == selectedCategoryName
                    matchesSearch && matchesMonth && matchesCategory
                }

                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text("Limpar Dados") },
                        text = { Text("Tem certeza que deseja apagar todas as transações?") },
                        confirmButton = {
                            TextButton(onClick = {
                                viewModel.clearAll()
                                showDeleteDialog = false
                            }) { Text("Confirmar") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
                        }
                    )
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        Column {
                            CenterAlignedTopAppBar(
                                title = { Text("Personal Finance") },
                                actions = {
                                    IconButton(onClick = { filePickerLauncher.launch("text/*") }) {
                                        Icon(Icons.Default.FileUpload, contentDescription = "Importar CSV")
                                    }
                                    IconButton(onClick = { showDeleteDialog = true }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Limpar Tudo")
                                    }
                                }
                            )
                            if (currentScreen == FinanceScreen.Transactions) {
                                SearchBar(query = searchQuery, onQueryChange = { searchQuery = it })
                                FilterSection(
                                    selectedMonth = selectedMonth,
                                    onMonthSelected = { selectedMonth = it },
                                    selectedCategory = selectedCategoryName,
                                    onCategorySelected = { selectedCategoryName = it },
                                    transactions = allTransactions,
                                    categories = allCategories
                                )
                            }
                        }
                    },
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.List, contentDescription = null) },
                                label = { Text("Transações") },
                                selected = currentScreen == FinanceScreen.Transactions,
                                onClick = { currentScreen = FinanceScreen.Transactions }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) },
                                label = { Text("Resumo") },
                                selected = currentScreen == FinanceScreen.Summary,
                                onClick = { currentScreen = FinanceScreen.Summary }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                                label = { Text("Gastos") },
                                selected = currentScreen == FinanceScreen.Breakdown,
                                onClick = { currentScreen = FinanceScreen.Breakdown }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                label = { Text("Categorias") },
                                selected = currentScreen == FinanceScreen.Categories,
                                onClick = { currentScreen = FinanceScreen.Categories }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        when (currentScreen) {
                            FinanceScreen.Transactions -> TransactionsListScreen(filteredTransactions, allCategories, viewModel)
                            FinanceScreen.Summary -> SummaryScreen(filteredTransactions)
                            FinanceScreen.Breakdown -> BreakdownScreen(filteredTransactions)
                            FinanceScreen.Categories -> CategoriesScreen(allCategories, viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterSection(
    selectedMonth: Int?,
    onMonthSelected: (Int?) -> Unit,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    transactions: List<Transaction>,
    categories: List<Category>
) {
    Column {
        MonthFilter(selectedMonth, onMonthSelected, transactions)
        CategoryFilter(selectedCategory, onCategorySelected, categories)
    }
}

@Composable
fun CategoryFilter(
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    categories: List<Category>
) {
    ScrollableTabRow(
        selectedTabIndex = if (selectedCategory == null) 0 else categories.indexOfFirst { it.name == selectedCategory } + 1,
        edgePadding = 16.dp,
        containerColor = Color.Transparent,
        divider = {}
    ) {
        Tab(
            selected = selectedCategory == null,
            onClick = { onCategorySelected(null) },
            text = { Text("Todas Categorias") }
        )
        categories.forEach { category ->
            Tab(
                selected = selectedCategory == category.name,
                onClick = { onCategorySelected(category.name) },
                text = { Text(category.name) }
            )
        }
    }
}

@Composable
fun TransactionsListScreen(
    transactions: List<Transaction>,
    categories: List<Category>,
    viewModel: FinanceViewModel
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Transações (${transactions.size})",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(transactions) { transaction ->
                TransactionItem(transaction, categories, viewModel)
            }
        }
    }
}

@Composable
fun CategoriesScreen(categories: List<Category>, viewModel: FinanceViewModel) {
    var newCategoryName by remember { mutableStateOf("") }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Gerenciar Categorias", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            TextField(
                value = newCategoryName,
                onValueChange = { newCategoryName = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Nova categoria") }
            )
            IconButton(onClick = {
                if (newCategoryName.isNotBlank()) {
                    viewModel.addCategory(newCategoryName)
                    newCategoryName = ""
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar")
            }
        }
        
        LazyColumn {
            items(categories, key = { it.id }) { category ->
                var isEditing by remember { mutableStateOf(false) }
                var editedName by remember { mutableStateOf(category.name) }
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isEditing) {
                        TextField(
                            value = editedName,
                            onValueChange = { editedName = it },
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            if (editedName.isNotBlank() && editedName != category.name) {
                                viewModel.updateCategory(category.name, category.copy(name = editedName))
                            }
                            isEditing = false
                        }) { Icon(Icons.Default.Check, contentDescription = "Salvar") }
                        IconButton(onClick = { 
                            isEditing = false 
                            editedName = category.name
                        }) { Icon(Icons.Default.Close, contentDescription = "Cancelar") }
                    } else {
                        Text(category.name, modifier = Modifier.weight(1f))
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
                        IconButton(onClick = { viewModel.deleteCategory(category) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Excluir")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryScreen(transactions: List<Transaction>) {
    val totalExpenses = transactions.filter { it.amount > 0 }.sumOf { it.amount }
    val totalCredits = transactions.filter { it.amount < 0 }.sumOf { it.amount }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SummaryCard(totalExpenses, totalCredits)
    }
}

@Composable
fun BreakdownScreen(transactions: List<Transaction>) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        CategoryBreakdown(transactions)
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text("Buscar transação...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        singleLine = true,
        shape = MaterialTheme.shapes.medium
    )
}

@Composable
fun MonthFilter(selectedMonth: Int?, onMonthSelected: (Int?) -> Unit, transactions: List<Transaction>) {
    val months = transactions.map { it.date.monthValue }.distinct().sorted()
    val locale = Locale.forLanguageTag("pt-BR")
    ScrollableTabRow(
        selectedTabIndex = if (selectedMonth == null) 0 else months.indexOf(selectedMonth) + 1,
        edgePadding = 16.dp,
        containerColor = Color.Transparent,
        divider = {}
    ) {
        Tab(selected = selectedMonth == null, onClick = { onMonthSelected(null) }, text = { Text("Todos Meses") })
        months.forEach { month ->
            Tab(
                selected = selectedMonth == month,
                onClick = { onMonthSelected(month) },
                text = { 
                    val monthName = java.time.Month.of(month).getDisplayName(java.time.format.TextStyle.SHORT, locale)
                    Text(monthName.replaceFirstChar { it.uppercase() }) 
                }
            )
        }
    }
}

@Composable
fun CategoryBreakdown(transactions: List<Transaction>) {
    val expensesByCategory = transactions
        .filter { it.amount > 0 }
        .groupBy { it.categoryName }
        .mapValues { it.value.sumOf { t -> t.amount } }
        .toList().sortedByDescending { it.second }

    if (expensesByCategory.isNotEmpty()) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Gastos por Categoria", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                expensesByCategory.forEach { (categoryName, amount) ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(categoryName, fontSize = 16.sp)
                        Text("R$ ${"%.2f".format(amount)}", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(expenses: Double, credits: Double) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Resumo Financeiro", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                SummaryItem("Saídas", expenses, Color(0xFFE57373))
                SummaryItem("Entradas", -credits, Color(0xFF81C784))
            }
            val balance = -credits - expenses
            Text("Saldo: R$ ${"%.2f".format(balance)}", fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp), color = if (balance >= 0) Color(0xFF388E3C) else Color(0xFFD32F2F))
        }
    }
}

@Composable
fun SummaryItem(label: String, value: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 14.sp)
        Text("R$ ${"%.2f".format(value)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun TransactionItem(transaction: Transaction, categories: List<Category>, viewModel: FinanceViewModel) {
    var showSelectionDialog by remember { mutableStateOf(false) }
    
    if (showSelectionDialog) {
        AlertDialog(
            onDismissRequest = { showSelectionDialog = false },
            title = { Text("Alterar Categoria") },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                    items(categories) { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateTransactionCategory(transaction, category.name)
                                    showSelectionDialog = false
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = transaction.categoryName == category.name,
                                onClick = null 
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(category.name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSelectionDialog = false }) { Text("Fechar") }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showSelectionDialog = true }
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(transaction.categoryName.take(1), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.title, fontWeight = FontWeight.Medium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(transaction.categoryName, fontSize = 12.sp, color = Color.Gray)
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.padding(start = 4.dp).size(12.dp),
                        tint = Color.Gray
                    )
                }
            }
            Text(
                text = "R$ ${"%.2f".format(transaction.amount)}",
                fontWeight = FontWeight.Bold,
                color = if (transaction.amount > 0) Color(0xFFD32F2F) else Color(0xFF388E3C)
            )
        }
    }
}
