package common.util.validate

import android.widget.CheckBox
import android.widget.EditText

object ValidateCheckboxState {

    fun checkboxValidate(checkBox: CheckBox, passwordEditText: EditText) {
        passwordEditText.transformationMethod =
            android.text.method.PasswordTransformationMethod.getInstance()

        checkBox.setOnCheckedChangeListener { _, isChecked ->
            passwordEditText.transformationMethod =
                if (isChecked) null else android.text.method.PasswordTransformationMethod.getInstance()

            passwordEditText.setSelection(passwordEditText.text?.length ?: 0)

        }
    }
}