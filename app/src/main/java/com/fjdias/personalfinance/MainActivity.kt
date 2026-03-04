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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fjdias.personalfinance.ui.theme.PersonalFinanceTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.abs

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
                var selectedYear by remember { mutableStateOf<Int?>(null) }
                var selectedMonth by remember { mutableStateOf<Int?>(null) }
                var selectedCategoryName by remember { mutableStateOf<String?>(null) }
                var showDeleteDialog by remember { mutableStateOf(false) }
                var showExportDialog by remember { mutableStateOf(false) }
                var showAddTransactionDialog by remember { mutableStateOf(false) }
                var currentScreen by remember { mutableStateOf(FinanceScreen.Transactions) }

                val filePickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    uri?.let { viewModel.importCsv(context, it) }
                }

                // Launcher para criar o arquivo CSV
                val exportLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.CreateDocument("text/csv")
                ) { uri: Uri? ->
                    uri?.let { 
                        viewModel.exportCsv(context, it, allTransactions)
                    }
                }

                val transactionsForList = allTransactions.filter {
                    val matchesSearch = it.title.contains(searchQuery, ignoreCase = true)
                    val matchesYear = selectedYear == null || it.date.year == selectedYear
                    val matchesMonth = selectedMonth == null || it.date.monthValue == selectedMonth
                    val matchesCategory = selectedCategoryName == null || it.categoryName == selectedCategoryName
                    matchesSearch && matchesYear && matchesMonth && matchesCategory
                }

                val transactionsForAnalytics = allTransactions.filter {
                    val matchesYear = selectedYear == null || it.date.year == selectedYear
                    val matchesMonth = selectedMonth == null || it.date.monthValue == selectedMonth
                    matchesYear && matchesMonth
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

                if (showExportDialog) {
                    var fileName by remember { mutableStateOf("backup_finance_${LocalDate.now()}.csv") }
                    AlertDialog(
                        onDismissRequest = { showExportDialog = false },
                        title = { Text("Exportar Backup") },
                        text = {
                            Column {
                                Text("Escolha o nome do arquivo:")
                                Spacer(modifier = Modifier.height(8.dp))
                                TextField(
                                    value = fileName,
                                    onValueChange = { fileName = it },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                // Primeiro lança o seletor, depois fecha o diálogo
                                exportLauncher.launch(fileName)
                                showExportDialog = false
                            }) { Text("Escolher Local") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showExportDialog = false }) { Text("Cancelar") }
                        }
                    )
                }

                if (showAddTransactionDialog) {
                    TransactionDialog(
                        title = "Nova Transação",
                        categories = allCategories,
                        onDismiss = { showAddTransactionDialog = false },
                        onConfirm = { t, a, c, d ->
                            viewModel.addManualTransaction(t, a, c, d)
                            showAddTransactionDialog = false
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
                                    IconButton(onClick = { showExportDialog = true }) {
                                        Icon(Icons.Default.SaveAlt, contentDescription = "Exportar Backup")
                                    }
                                    IconButton(onClick = { filePickerLauncher.launch("text/*") }) {
                                        Icon(Icons.Default.FileUpload, contentDescription = "Importar CSV")
                                    }
                                    IconButton(onClick = { showDeleteDialog = true }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Limpar Tudo")
                                    }
                                }
                            )
                            
                            if (currentScreen != FinanceScreen.Categories) {
                                YearFilter(
                                    selectedYear = selectedYear,
                                    onYearSelected = { 
                                        selectedYear = it
                                        selectedMonth = null 
                                    },
                                    transactions = allTransactions
                                )
                                MonthFilter(
                                    selectedMonth = selectedMonth,
                                    selectedYear = selectedYear,
                                    onMonthSelected = { selectedMonth = it },
                                    transactions = allTransactions
                                )
                            }

                            if (currentScreen == FinanceScreen.Transactions) {
                                SearchBar(query = searchQuery, onQueryChange = { searchQuery = it })
                                CategoryFilter(
                                    selectedCategory = selectedCategoryName,
                                    onCategorySelected = { selectedCategoryName = it },
                                    categories = allCategories
                                )
                            }
                        }
                    },
                    floatingActionButton = {
                        if (currentScreen == FinanceScreen.Transactions) {
                            FloatingActionButton(onClick = { showAddTransactionDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = "Adicionar Transação")
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
                            FinanceScreen.Transactions -> TransactionsListScreen(transactionsForList, allCategories, viewModel)
                            FinanceScreen.Summary -> SummaryScreen(transactionsForAnalytics)
                            FinanceScreen.Breakdown -> BreakdownScreen(transactionsForAnalytics)
                            FinanceScreen.Categories -> CategoriesScreen(allCategories, viewModel)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDialog(
    title: String,
    initialTransaction: Transaction? = null,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String, LocalDate) -> Unit
) {
    var titleText by remember { mutableStateOf(initialTransaction?.title ?: "") }
    var amountText by remember { mutableStateOf(initialTransaction?.amount?.let { abs(it).toString() } ?: "") }
    var selectedCategory by remember { mutableStateOf(initialTransaction?.categoryName ?: "Outros") }
    var selectedDate by remember { mutableStateOf(initialTransaction?.date ?: LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showCategoryDropdown by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(value = titleText, onValueChange = { titleText = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
                TextField(value = amountText, onValueChange = { amountText = it }, label = { Text("Valor (R$)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Data: ${selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}")
                }
                Box(Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { showCategoryDropdown = true }, modifier = Modifier.fillMaxWidth()) { Text("Categoria: $selectedCategory") }
                    DropdownMenu(expanded = showCategoryDropdown, onDismissRequest = { showCategoryDropdown = false }) {
                        categories.forEach { category ->
                            DropdownMenuItem(text = { Text(category.name) }, onClick = { selectedCategory = category.name; showCategoryDropdown = false })
                        }
                        if (categories.none { it.name == "Outros" }) {
                            DropdownMenuItem(text = { Text("Outros") }, onClick = { selectedCategory = "Outros"; showCategoryDropdown = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val value = amountText.toDoubleOrNull() ?: 0.0
                if (titleText.isNotBlank() && value != 0.0) {
                    onConfirm(titleText, value, selectedCategory, selectedDate)
                }
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun YearFilter(selectedYear: Int?, onYearSelected: (Int?) -> Unit, transactions: List<Transaction>) {
    val years = transactions.map { it.date.year }.distinct().sortedDescending()
    ScrollableTabRow(
        selectedTabIndex = if (selectedYear == null) 0 else years.indexOf(selectedYear) + 1,
        edgePadding = 16.dp, containerColor = Color.Transparent, divider = {}
    ) {
        Tab(selected = selectedYear == null, onClick = { onYearSelected(null) }, text = { Text("Todos Anos") })
        years.forEach { year ->
            Tab(selected = selectedYear == year, onClick = { onYearSelected(year) }, text = { Text(year.toString()) })
        }
    }
}

@Composable
fun MonthFilter(selectedMonth: Int?, selectedYear: Int?, onMonthSelected: (Int?) -> Unit, transactions: List<Transaction>) {
    val filteredTransactions = if (selectedYear != null) {
        transactions.filter { it.date.year == selectedYear }
    } else {
        transactions
    }
    val months = filteredTransactions.map { it.date.monthValue }.distinct().sorted()
    val locale = Locale.forLanguageTag("pt-BR")
    ScrollableTabRow(
        selectedTabIndex = if (selectedMonth == null) 0 else months.indexOf(selectedMonth) + 1,
        edgePadding = 16.dp, containerColor = Color.Transparent, divider = {} 
    ) {
        Tab(selected = selectedMonth == null, onClick = { onMonthSelected(null) }, text = { Text("Todos Meses") })
        months.forEach { month ->
            Tab(selected = selectedMonth == month, onClick = { onMonthSelected(month) }, text = { 
                val monthName = java.time.Month.of(month).getDisplayName(java.time.format.TextStyle.SHORT, locale)
                Text(monthName.replaceFirstChar { it.uppercase() }) 
            })
        }
    }
}

@Composable
fun CategoryFilter(selectedCategory: String?, onCategorySelected: (String?) -> Unit, categories: List<Category>) {
    ScrollableTabRow(
        selectedTabIndex = if (selectedCategory == null) 0 else categories.indexOfFirst { it.name == selectedCategory } + 1,
        edgePadding = 16.dp, containerColor = Color.Transparent, divider = {}
    ) {
        Tab(selected = selectedCategory == null, onClick = { onCategorySelected(null) }, text = { Text("Todas Categorias") })
        categories.forEach { category ->
            Tab(selected = selectedCategory == category.name, onClick = { onCategorySelected(category.name) }, text = { Text(category.name) })
        }
    }
}

@Composable
fun TransactionsListScreen(transactions: List<Transaction>, categories: List<Category>, viewModel: FinanceViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Transações (${transactions.size})", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
            TextField(value = newCategoryName, onValueChange = { newCategoryName = it }, modifier = Modifier.weight(1f), placeholder = { Text("Nova categoria") })
            IconButton(onClick = { if (newCategoryName.isNotBlank()) { viewModel.addCategory(newCategoryName); newCategoryName = "" } }) { Icon(Icons.Default.Add, null) }
        }
        LazyColumn {
            items(categories, key = { it.id }) { category ->
                var isEditing by remember { mutableStateOf(false) }
                var editedName by remember { mutableStateOf(category.name) }
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (isEditing) {
                        TextField(value = editedName, onValueChange = { editedName = it }, modifier = Modifier.weight(1f))
                        IconButton(onClick = { if (editedName.isNotBlank() && editedName != category.name) { viewModel.updateCategory(category.name, category.copy(name = editedName)) }; isEditing = false }) { Icon(Icons.Default.Check, null) }
                        IconButton(onClick = { isEditing = false; editedName = category.name }) { Icon(Icons.Default.Close, null) }
                    } else {
                        Text(category.name, modifier = Modifier.weight(1f))
                        if (category.name != "Renda") {
                            IconButton(onClick = { isEditing = true }) { Icon(Icons.Default.Edit, null) }
                            IconButton(onClick = { viewModel.deleteCategory(category) }) { Icon(Icons.Default.Delete, null) }
                        } else {
                            Icon(Icons.Default.Lock, contentDescription = "Categoria Fixa", modifier = Modifier.padding(12.dp).size(18.dp), tint = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryScreen(transactions: List<Transaction>) {
    val income = transactions.filter { it.categoryName == "Renda" }.sumOf { abs(it.amount) }
    val expenses = transactions.filter { it.categoryName != "Renda" }.sumOf { abs(it.amount) }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) { SummaryCard(expenses, -income) }
}

@Composable
fun BreakdownScreen(transactions: List<Transaction>) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) { CategoryBreakdown(transactions) }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    TextField(value = query, onValueChange = onQueryChange, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), placeholder = { Text("Buscar transação...") }, leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true, shape = MaterialTheme.shapes.medium)
}

@Composable
fun CategoryBreakdown(transactions: List<Transaction>) {
    val expensesByCategory = transactions.filter { it.categoryName != "Renda" }.groupBy { it.categoryName }.mapValues { it.value.sumOf { t -> abs(t.amount) } }.toList().sortedByDescending { it.second }
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
    var showEditDialog by remember { mutableStateOf(false) }
    if (showEditDialog) {
        TransactionDialog(
            title = "Editar Transação",
            initialTransaction = transaction,
            categories = categories,
            onDismiss = { showEditDialog = false },
            onConfirm = { t, a, c, d ->
                viewModel.updateTransaction(transaction.copy(title = t, amount = a, categoryName = c, date = d))
                showEditDialog = false
            }
        )
    }
    Card(modifier = Modifier.fillMaxWidth().clickable { showEditDialog = true }) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                Text(transaction.categoryName.take(1), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.title, fontWeight = FontWeight.Medium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(transaction.categoryName, fontSize = 12.sp, color = Color.Gray)
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.padding(start = 4.dp).size(12.dp), tint = Color.Gray)
                }
            }
            Text(text = "R$ ${"%.2f".format(transaction.amount)}", fontWeight = FontWeight.Bold, color = if (transaction.categoryName == "Renda") Color(0xFF388E3C) else Color(0xFFD32F2F) )
        }
    }
}
