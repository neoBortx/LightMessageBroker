package com.bortxapps.lightmessagebrokerexample.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.bortxapps.lightmessagebrokerexample.R
import com.bortxapps.lightmessagebrokerexample.activity.ui.theme.LightMessageBrokerExampleTheme
import com.bortxapps.lightmessagebrokerexample.viewmodel.ActivityState
import com.bortxapps.lightmessagebrokerexample.viewmodel.ActivityViewModel
import dagger.hilt.android.AndroidEntryPoint

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: ActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.register(lifecycle = lifecycle)

        setContent {
            LightMessageBrokerExampleTheme {
                Scaffold(
                    topBar = { TopAppBarCustom() }) { contentPadding ->
                    Box(
                        modifier = Modifier
                            .padding(contentPadding)
                            .fillMaxHeight()
                            .fillMaxWidth()
                    ) {
                        ConstraintLayoutContent(
                            state = viewModel.uiState,
                            onNumberMessagesChanged = viewModel::setMessages,
                            onNumberConsumersChanged = viewModel::setConsumers,
                            onStart = viewModel::start,
                            onMessagesRequested = viewModel::getConsumerMessages,
                            onSendByClientIdChanged = viewModel::onSendByClientIdChanged
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopAppBarCustom() {
    SmallTopAppBar(
        title = { Text(stringResource(id = R.string.app_name)) },
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = colorResource(id = R.color.purple_500),
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            titleContentColor = colorResource(id = R.color.white),
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
        )
    )
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ConstraintLayoutContent(
    state: ActivityState,
    onNumberMessagesChanged: (String) -> Unit,
    onNumberConsumersChanged: (String) -> Unit,
    onStart: () -> Unit,
    onMessagesRequested: (Long) -> List<Int>,
    onSendByClientIdChanged: (Boolean) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    ConstraintLayout(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(color = colorResource(id = R.color.white))

    ) {
        val (numberMessagesTextEdit, consumersTextEdit, startButton, resultList, progressBar, checkbox) = createRefs()
        val pattern = remember { Regex("\\d*") }

        TextField(
            value = state.numberMessages,
            onValueChange = { if (it.matches(pattern)) onNumberMessagesChanged(it) },
            placeholder = { Text(stringResource(R.string.number_messages)) },
            enabled = !state.isRunning,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { keyboardController?.hide() }),

            colors = TextFieldDefaults.textFieldColors(
                placeholderColor = colorResource(id = R.color.purple_500),
                focusedIndicatorColor = colorResource(id = R.color.purple_700),
                unfocusedIndicatorColor = colorResource(id = R.color.purple_500),
                containerColor = colorResource(id = R.color.text_field_background),
                textColor = colorResource(id = R.color.purple_500),
                disabledTextColor = colorResource(id = R.color.purple_200)
            ),
            modifier = Modifier
                .constrainAs(numberMessagesTextEdit) {
                    top.linkTo(parent.top, margin = 30.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        )

        TextField(
            value = state.numberConsumers,
            onValueChange = { if (it.matches(pattern)) onNumberConsumersChanged(it) },
            placeholder = { Text(stringResource(R.string.number_consumers)) },
            enabled = !state.isRunning,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { keyboardController?.hide() }),

            colors = TextFieldDefaults.textFieldColors(
                placeholderColor = colorResource(id = R.color.purple_500),
                focusedIndicatorColor = colorResource(id = R.color.purple_700),
                unfocusedIndicatorColor = colorResource(id = R.color.purple_500),
                containerColor = colorResource(id = R.color.text_field_background),
                textColor = colorResource(id = R.color.purple_500),
                disabledTextColor = colorResource(id = R.color.purple_200)
            ),
            modifier = Modifier
                .constrainAs(consumersTextEdit) {
                    top.linkTo(numberMessagesTextEdit.bottom, margin = 20.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        )

        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .constrainAs(checkbox) {
                    top.linkTo(consumersTextEdit.bottom, margin = 10.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }) {
            Text(
                text = "Not use categories",
                color = colorResource(id = R.color.purple_500)
            )
            Checkbox(checked = state.sendByClientId, onCheckedChange = onSendByClientIdChanged)
        }

        Button(
            onClick = {
                keyboardController?.hide()
                onStart()
            },
            enabled = !state.isRunning,
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.purple_500),
                disabledContainerColor = colorResource(id = R.color.purple_200),
            ),
            modifier = Modifier.constrainAs(startButton) {
                top.linkTo(checkbox.bottom, margin = 10.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        ) {
            Text(
                color = colorResource(id = R.color.white),
                text = stringResource(R.string.start)
            )
        }

        AnimatedVisibility(visible = state.isRunning,
            modifier = Modifier.constrainAs(progressBar) {
                top.linkTo(startButton.bottom, margin = 16.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }) {
            CircularProgressIndicator(
                color = colorResource(id = R.color.purple_500)
            )
        }

        AnimatedVisibility(visible = state.showResult,
            modifier = Modifier
                .constrainAs(resultList) {
                    top.linkTo(startButton.bottom, margin = 16.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                    height = Dimension.fillToConstraints
                }
                .fillMaxWidth()
        ) {

            Column(
                Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                Text(
                    modifier = Modifier.padding(bottom = 10.dp),
                    color = colorResource(id = R.color.purple_500),
                    text = stringResource(R.string.all_messages_label) + " " + state.elapsedTime + " " + stringResource(R.string.milliseconds)
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                ) {
                    items(state.result.toList()) {
                        var showMessages by remember { mutableStateOf(false) }
                        Card(
                            modifier = Modifier
                                .padding(vertical = 5.dp)
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            onClick = { showMessages = !showMessages },
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Client: ${it.first}",
                                    color = colorResource(id = R.color.purple_500)
                                )
                                Text(
                                    text = "Process ${it.second} events",
                                    color = colorResource(id = R.color.purple_500)
                                )

                                if (!showMessages) {
                                    Icon(
                                        modifier = Modifier.fillMaxWidth(),
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "",
                                        tint = colorResource(id = R.color.purple_500)
                                    )
                                } else {
                                    Icon(
                                        modifier = Modifier.fillMaxWidth(),
                                        imageVector = Icons.Default.KeyboardArrowUp,
                                        contentDescription = "",
                                        tint = colorResource(id = R.color.purple_500)
                                    )
                                }

                                AnimatedVisibility(visible = showMessages) {
                                    Text(
                                        text = "Processed messages: ${onMessagesRequested(it.first)}",
                                        color = colorResource(id = R.color.purple_500)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {

    LightMessageBrokerExampleTheme {
        Scaffold(topBar = { TopAppBarCustom() }) { contentPadding ->
            Box(
                modifier = Modifier.padding(contentPadding)
            ) {
                ConstraintLayoutContent(
                    state = ActivityState(
                        isRunning = false,
                        showResult = true,
                        numberMessages = "5000",
                        numberConsumers = "5",
                        elapsedTime = "7565",
                        result = mapOf(1L to 10, 2L to 50, 3L to 5, 4L to 24, 5L to 36, 6L to 1000, 7L to 5),
                        false
                    ),
                    onNumberMessagesChanged = {},
                    onNumberConsumersChanged = {},
                    onStart = {},
                    onMessagesRequested = { listOf() },
                    onSendByClientIdChanged = {}
                )
            }
        }
    }
}