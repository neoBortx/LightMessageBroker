package com.bortxapps.lightmessagebrokerexample.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.bortxapps.lightmessagebrokerexample.R
import com.bortxapps.lightmessagebrokerexample.activity.ui.theme.LightMessageBrokerExampleTheme
import com.bortxapps.lightmessagebrokerexample.viewmodel.ActivityState
import com.bortxapps.lightmessagebrokerexample.viewmodel.ActivityViewModel
import dagger.hilt.android.AndroidEntryPoint

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class MainActivity() : ComponentActivity() {

    private val viewModel: ActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.register(lifecycle = lifecycle)

        setContent {
            LightMessageBrokerExampleTheme {
                Scaffold(topBar = { TopAppBarCustom() }) { contentPadding ->
                    Box(
                        modifier = Modifier.padding(contentPadding)
                    ) {
                        ConstraintLayoutContent(
                            state = viewModel.uiState,
                            onNumberMessagesChanged = viewModel::setMessages,
                            onStart = viewModel::start
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ConstraintLayoutContent(
    state: ActivityState,
    onNumberMessagesChanged: (String) -> Unit,
    onStart: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    ConstraintLayout(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(color = colorResource(id = R.color.white))

    ) {
        val (numberMessagesTextEdit, startButton, text, progressBar) = createRefs()
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
            modifier = Modifier.constrainAs(numberMessagesTextEdit) {
                top.linkTo(parent.top, margin = 50.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            },
        )

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
                top.linkTo(numberMessagesTextEdit.bottom, margin = 16.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        ) {
            Text(
                color = colorResource(id = R.color.white),
                text = stringResource(R.string.start)
            )
        }

        if (state.isRunning) {
            CircularProgressIndicator(
                color = colorResource(id = R.color.purple_500),
                modifier = Modifier.constrainAs(progressBar) {
                    top.linkTo(startButton.bottom, margin = 16.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                })
        }

        if (state.showResult) {
            // Assign reference "text" to the Text composable
            // and constrain it to the bottom of the Button composable
            Text(
                color = colorResource(id = R.color.purple_500),
                text = stringResource(R.string.all_messages_label) + " " + state.elapsedTime + " " + stringResource(R.string.milliseconds),
                modifier = Modifier.constrainAs(text) {
                    top.linkTo(startButton.bottom, margin = 16.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                })
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
                    state = ActivityState.getInitial(),
                    onNumberMessagesChanged = {},
                    onStart = {}
                )
            }
        }
    }
}