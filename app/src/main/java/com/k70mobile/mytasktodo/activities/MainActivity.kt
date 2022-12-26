package com.k70mobile.mytasktodo.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.k70mobile.mytasktodo.R
import com.k70mobile.mytasktodo.adapters.BoardItemsAdapter
import com.k70mobile.mytasktodo.databinding.ActivityMainBinding
import com.k70mobile.mytasktodo.firebase.FirestoreDB
import com.k70mobile.mytasktodo.models.Board
import com.k70mobile.mytasktodo.models.User
import com.k70mobile.mytasktodo.utils.Constants
import de.hdodenhof.circleimageview.CircleImageView

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var binding: ActivityMainBinding? = null
    private lateinit var userName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupActionbar()

        binding?.navView?.setNavigationItemSelectedListener(this)

        FirestoreDB().loadUserData(this, readBoardsList = true)

        val fabCreateBoard = findViewById<FloatingActionButton>(R.id.fab_create_board)
        fabCreateBoard.setOnClickListener {
            // By this way, We don't have to get the username here again and
            // have another database request because what we try to do is to
            // have as few requests as possible.
            val intent = Intent(this, CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME, userName)
            startActivityForResult(intent, Constants.CREATE_BOARD_REQUEST_CODE)
        }
    }

    fun populateBoardsListToUI(boardsList: ArrayList<Board>) {
        hideProgressDialog()
        val rvBoardsList = findViewById<RecyclerView>(R.id.rv_boards_list)
        val llNoBoardAvailable = findViewById<LinearLayout>(R.id.ll_no_boards_available)

        if (boardsList.size > 0) {
            rvBoardsList.visibility = View.VISIBLE
            llNoBoardAvailable.visibility = View.GONE

            val layoutManager = LinearLayoutManager(this)
            rvBoardsList.layoutManager = layoutManager
            rvBoardsList.setHasFixedSize(true)

            val adapter = BoardItemsAdapter(this, boardsList)
            rvBoardsList.adapter = adapter
            Log.i("POPUP:", "Board adapter size: ${adapter.itemCount}")

            // Create a divider between two recycler view
            val dividerItemDecoration = DividerItemDecoration(rvBoardsList.context, layoutManager.orientation)
            rvBoardsList.addItemDecoration(dividerItemDecoration)

            adapter.setOnClickListenerAdapter(object: BoardItemsAdapter.OnClickListener{
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentedID)
                    startActivity(intent)
                }
            })
        } else {
            rvBoardsList.visibility = View.GONE
            llNoBoardAvailable.visibility = View.VISIBLE
        }

    }

    private fun setupActionbar() {
        val toolbarMainActivity = findViewById<Toolbar>(R.id.toolbar_main_activity)
        setSupportActionBar(toolbarMainActivity)
        toolbarMainActivity?.setNavigationIcon(R.drawable.ic_action_navigation_menu)

        toolbarMainActivity?.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

    // toggle is "ic_action_navigation_menu" icon
    private fun toggleDrawer() {

        // Check drawLayout is open or not, then execute open or close the navigation
        if (binding?.drawerLayout?.isDrawerOpen(GravityCompat.START) == true) {
            binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        } else {
            binding?.drawerLayout?.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (binding?.drawerLayout?.isDrawerOpen(GravityCompat.START) == true) {
            binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        } else {
            doubleBackToExit()
        }
    }

    // implement functionality that should execute once we click one of those navigation
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_my_profile -> {
                startActivityForResult(
                    Intent(this, MyProfileActivity::class.java),
                    Constants.MY_PROFILE_REQUEST_CODE
                )
            }
            R.id.nav_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, IntroActivity::class.java)
                /*
                When using this flag, if a task is already running for the activity you are now starting,
                then a new activity will not be started. Instead, the current task will simply
                be brought to the front of the screen with the state. It was last.

                Basically we're not starting a new activity for interactivity if there was an interactivity in
                our stack already. So in our activities stack and we're just getting the intro activity to the front
                 */
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                /*
                So start activity with intent and then finish the current activity so that the user can't get back to
                the main activity once we signed out.
                 */
                startActivity(intent)
                finish()
            }
        }
        binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        return true
    }

    fun updateNavigationUserDetails(user: User, readBoardsList: Boolean) {
        val navUserImage = findViewById<CircleImageView>(R.id.nav_user_image)
        val tvUsername = findViewById<TextView>(R.id.tv_username)
        userName = user.name

        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(navUserImage)

        tvUsername.text = user.name

        if (readBoardsList) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreDB().getBoardsList(this)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.MY_PROFILE_REQUEST_CODE) {
            FirestoreDB().loadUserData(this)
        } else if (resultCode == Activity.RESULT_OK && requestCode == Constants.CREATE_BOARD_REQUEST_CODE) {
            FirestoreDB().getBoardsList(this)
        } else {
            Log.e("Cancel", "Cancelled")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}