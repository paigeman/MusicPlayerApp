package org.edu.ncu.part1

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.login_tool.*

class LoginFragment : Fragment() {

    lateinit private var loginActivity: AppCompatActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //TODO: add code below
        loginActivity = activity as AppCompatActivity
        setTranslucent(loginActivity)
        loginActivity.setSupportActionBar(login_tool)
        login_tool.title = "登录"
        login_tool.setNavigationOnClickListener {
            replaceFragment(IndexFragment())
        }
        val dbHelper = DBUtils(loginActivity,"music.db",2)
        login_button.setOnClickListener {
            val db = dbHelper.writableDatabase
            var username = login_username.text.toString()
            var userpasswd = login_passwd.text.toString()
            if(username==""){
                Toast.makeText(loginActivity,"用户名不能为空", Toast.LENGTH_SHORT).show()
            }
            else if(userpasswd==""){
                Toast.makeText(loginActivity,"密码不能为空", Toast.LENGTH_SHORT).show()
            }
            else{
                val cursor = db.query("User", arrayOf("username","userpwd"),"username=?",
                    arrayOf(username),null,null,null)
                var userpwd = ""
                if(cursor.moveToFirst()){
                    userpwd = cursor.getString(cursor.getColumnIndex(("userpwd")))
                }
                if(userpwd==""){
                    Toast.makeText(loginActivity,"该用户不存在，请重新输入",Toast.LENGTH_SHORT).show()
                }
                else if(userpasswd!=userpwd){
                    Toast.makeText(loginActivity,"密码错误。请重新输入",Toast.LENGTH_SHORT).show()
                }
                else{
                    val fragment = IndexFragment()
                    val data = Bundle()
                    data.putInt("status",1)
                    data.putString("username",username)
                    fragment.arguments = data
                    replaceFragment(fragment)
                }
            }
        }
        login_register.setOnClickListener {
            replaceFragment(RegisterFragment())
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
            val temp = activity.findViewById<View>(R.id.login_content) as ViewGroup
            val rootView = temp.getChildAt(0) as ViewGroup
            rootView.fitsSystemWindows = true
            rootView.clipToPadding = true
        }
    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = loginActivity.supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.replace,fragment)
        transaction.commit()
    }

}