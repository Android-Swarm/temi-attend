package com.zetzaus.temiattend.ui

import android.content.Context
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zetzaus.temiattend.R
import com.zetzaus.temiattend.database.CryptoManager
import com.zetzaus.temiattend.database.PreferenceRepository
import com.zetzaus.temiattend.ext.LOG_TAG
import com.zetzaus.temiattend.ext.isMixedCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class PasswordFragmentViewModel @ViewModelInject constructor(
    @ApplicationContext private val context: Context,
    repository: PreferenceRepository
) : PreferenceViewModel(repository) {

    private val _repeatPasswordError = MutableLiveData<String>()
    val repeatPasswordError: LiveData<String> = _repeatPasswordError

    private val _passwordError = MutableLiveData<String>()
    val passwordError: LiveData<String> = _passwordError

    private val _oldPasswordError = MutableLiveData<String>()
    val oldPasswordError: LiveData<String> = _oldPasswordError

    private val _requirementVisible = MutableLiveData(Pair(true, true))
    val requirementVisible: LiveData<Pair<Boolean, Boolean>> =
        _requirementVisible // Length, Mixed case

    private val toVerify = ConflatedBroadcastChannel<String>()
    val verification = toVerify.asFlow()
        .combine(adminPassword) { submitted, (encrypted, iv) ->
            Log.d(LOG_TAG, "Encrypted: $encrypted\nIV:$iv")
            submitted == CryptoManager.decrypt(encrypted, iv)
        }

    fun updatePasswordRequirementDisplay(password: String) {
        _requirementVisible.value = Pair(
            !passwordLengthEnough(password),
            !password.isMixedCase()
        )
    }

    fun isValidPassword(password: String) = passwordLengthEnough(password) && password.isMixedCase()
        .also {
            if (it) Log.d(LOG_TAG, "Password is valid")
            else Log.d(LOG_TAG, "Password is not valid")

            if (!it) _passwordError.value = context.getString(R.string.error_weak_password)
        }

    fun isValidConfirmPassword(password: String, confirmPassword: String) =
        (password == confirmPassword).also {
            if (!it) _repeatPasswordError.value =
                context.getString(R.string.error_repeat_password_different)
        }

    fun tellWrongPassword(operation: PasswordOperation) {
        if (operation == PasswordOperation.INPUT_PASSWORD) {
            _passwordError.value = context.getString(R.string.error_wrong_password)
        } else if (operation == PasswordOperation.CHANGE_PASSWORD) {
            _oldPasswordError.value = context.getString(R.string.error_wrong_password)
        }
    }

    fun submitPasswordToVerify(password: String) = viewModelScope.launch { toVerify.send(password) }

    private fun passwordLengthEnough(password: String) =
        password.length >= context.resources.getInteger(R.integer.min_password_length)
}