package com.example.datastoreapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.datastoreapp.ui.theme.DataStore_AppTheme
import kotlinx.coroutines.flow.map
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private val Context.dataStore by preferencesDataStore("app_preferences")

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DataStore_AppTheme {
                // A surface container using the 'background' color from the theme

                val scope = rememberCoroutineScope()
                val context = LocalContext.current

                Scaffold(
                    topBar = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(color = MaterialTheme.colorScheme.primary)
                                .padding(12.dp), contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Receiver App",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(it),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        TwoForms(saveKey = { key, value ->
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    setValue(key = key, value = value)
                                }
                                Toast.makeText(
                                    context,
                                    "$value saved with key $key",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }, getValue = { key ->
                            runBlocking {
                                val str = getValue(key) ?: ""
                                Toast.makeText(
                                    context,
                                    "$str",
                                    Toast.LENGTH_LONG
                                ).show()
                                str
                            }
                        }

                        )
                    }
                }
            }
        }
    }

    suspend fun setValue(key: String, value: String) {
        dataStore.edit {
            it[stringPreferencesKey(key)] = value
            Log.d("DATA_STORE", "$value saved with key $key")
        }
    }


    suspend fun getValue(key: String): String? {
        return try {
            val preferencesKey = stringPreferencesKey(key)
            val value = dataStore.data.map { preferences ->
                preferences[preferencesKey]
            }.first()
            value
        } catch (e: Exception) {
            null
        }
    }
}


@Composable
fun TwoForms(
    saveKey: (String, String) -> Unit,
    getValue: (String) -> String
) {
    var key1 by remember { mutableStateOf("") }
    var value1 by remember { mutableStateOf("") }
    var name2 by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Form1(key1, value1) { key, value ->
            saveKey(key, value)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Form2(name2) { newName ->
            getValue(newName)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Form1(
    initialKey: String,
    initialValue: String,
    onFormSubmitted: (String, String) -> Unit
) {
    var key by remember { mutableStateOf(initialKey) }
    var value by remember { mutableStateOf(initialValue) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            OutlinedTextField(
                value = key,
                onValueChange = {
                    key = it
                },
                label = { Text("Key") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = value,
                onValueChange = {
                    value = it
                },
                label = { Text("Value") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onFormSubmitted(key, value) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Save in DataStore")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Form2(
    initialKey: String,
    onFormSubmitted: (String) -> String
) {
    var key by remember { mutableStateOf(initialKey) }
    var value by remember { mutableStateOf("") }
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            OutlinedTextField(
                value = key,
                onValueChange = {
                    key = it
                },
                label = { Text("Key") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val x = onFormSubmitted(key)
                    value = x
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Get from DataStore")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
