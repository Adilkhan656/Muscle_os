package com.musclesOS.adil.model

import android.telecom.Call
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult

class FacebookAuthManager(
    private val activity: AppCompatActivity
) {

    private val callbackManager = CallbackManager.Factory.create()
    fun getCallbackManager (): CallbackManager = callbackManager
    fun login(
        onSuccess :(String) -> Unit,
        onError:(Exception) -> Unit
    ){
        LoginManager.getInstance().registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult>{
                override fun onCancel() {
                    Toast.makeText(activity, "Login canceled", Toast.LENGTH_SHORT).show()
                }

                override fun onError(error: FacebookException) {
                 onError(error)
                }

                override fun onSuccess(result: LoginResult) {
                    onSuccess(result.accessToken.token)
                }

            }
        )
        LoginManager.getInstance().logInWithReadPermissions(activity
        ,listOf("public_profile"))
    }
}