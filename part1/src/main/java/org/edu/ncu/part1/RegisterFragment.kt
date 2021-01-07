package org.edu.ncu.part1

import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.fragment_register.*
import kotlinx.android.synthetic.main.login_tool.*

class RegisterFragment : Fragment() {

    lateinit private var registerActivity: AppCompatActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //TODO: add code below
        registerActivity = activity as AppCompatActivity
        setTranslucent(registerActivity)
        registerActivity.setSupportActionBar(login_tool)
        login_tool.title = "注册"
        login_tool.setNavigationOnClickListener {
            replaceFragment(LoginFragment())
        }
        val dbHelper = DBUtils(registerActivity,"music.db",2)
        register_button.setOnClickListener {
            val db = dbHelper.writableDatabase
            var username = register_username.text.toString()
            var userpasswd = register_passwd.text.toString()
            var userpasswd_confirm = register_passwd_confirm.text.toString()
            if(username==""){
                Toast.makeText(registerActivity,"用户名不能为空",Toast.LENGTH_SHORT).show()
            }
            else if(userpasswd==""){
                Toast.makeText(registerActivity,"密码不能为空",Toast.LENGTH_SHORT).show()
            }
            else if(userpasswd_confirm==""){
                Toast.makeText(registerActivity,"确认密码不能为空",Toast.LENGTH_SHORT).show()
            }
            else if(userpasswd!=userpasswd_confirm){
                Toast.makeText(registerActivity,"两次输入的密码不一致，请重新输入",Toast.LENGTH_SHORT).show()
            }
            else{
                val values = ContentValues().apply {
                    put("username",username)
                    put("userpwd",userpasswd)
                }
                db.insert("User",null,values)
                replaceFragment(LoginFragment())
            }
        }
    }

    private fun setTranslucent(activity: AppCompatActivity){
        //Build.VERSION.SDK_INT
        //The SDK version of the software currently running on this hardware device
        //Build.VERSION_CODES.KITKAT
        //Enumeration of the currently known SDK version codes.
        //October 2013: Android 4.4, KitKat, another tasty treat.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            //设置状态栏透明
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            //设置根布局的参数
            val temp = activity.findViewById<View>(R.id.register_content) as ViewGroup
            val rootView = temp.getChildAt(0) as ViewGroup
            rootView.fitsSystemWindows = true
            rootView.clipToPadding = true
        }
    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = registerActivity.supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.replace,fragment)
        transaction.commit()
    }

}