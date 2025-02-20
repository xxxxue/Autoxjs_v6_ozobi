package org.autojs.autoxjs.ui.floating.layoutinspector

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.lifecycle.ViewTreeViewModelStoreOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.stardust.app.DialogUtils
import com.stardust.enhancedfloaty.FloatyService
import com.stardust.view.accessibility.NodeInfo
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.autojs.autojs.ui.floating.layoutinspector.LayoutHierarchyView
import org.autojs.autoxjs.R
import org.autojs.autoxjs.ui.codegeneration.CodeGenerateDialog
import org.autojs.autoxjs.ui.compose.theme.AutoXJsTheme
import org.autojs.autoxjs.ui.floating.FloatyWindowManger
import org.autojs.autoxjs.ui.floating.FullScreenFloatyWindow
import org.autojs.autoxjs.ui.floating.MyLifecycleOwner
import org.autojs.autoxjs.ui.main.drawer.isNightModeNormal
import org.autojs.autoxjs.ui.widget.BubblePopupMenu
import org.autojs.autoxjs.ui.widget.OnItemClickListener
import pl.openrnd.multilevellistview.ItemInfo
import pl.openrnd.multilevellistview.MultiLevelListView
import kotlin.math.roundToInt

/**
 * Created by Stardust on 2017/3/12.
 */
open class LayoutHierarchyFloatyWindow(private val mRootNode: NodeInfo) : FullScreenFloatyWindow() {
    private var mLayoutHierarchyView: LayoutHierarchyView? = null
    private var mNodeInfoDialog: MaterialDialog? = null
    private var mBubblePopMenu: BubblePopupMenu? = null
    private var mNodeInfoView: NodeInfoView? = null
    private var mContext: Context? = null
    private var mSelectedNode: NodeInfo? = null
    private var nightMode = false

    override fun onCreateView(floatyService: FloatyService): View {
        mContext = ContextThemeWrapper(floatyService, R.style.AppTheme)
        nightMode = isNightModeNormal(mContext)
        // Added by ozobi - 2025/02/19
        BubblePopupMenu.nightMode = nightMode
        NodeInfoView.nightMode = nightMode
        LayoutHierarchyView.nightMode = nightMode
        // <
        mLayoutHierarchyView = LayoutHierarchyView(mContext)

        val view = ComposeView(mContext!!).apply {
            isFocusableInTouchMode = true
            setOnKeyListener { view, i, event ->
                if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP ) {
                    close()
                    return@setOnKeyListener true
                }
                return@setOnKeyListener false
            }
        }

        view.setContent {
            AutoXJsTheme {
                Content()
            }
        }
        // Trick The ComposeView into thinking we are tracking lifecycle
        val viewModelStore = ViewModelStore()
        val lifecycleOwner = MyLifecycleOwner()
        lifecycleOwner.performRestore(null)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        ViewTreeLifecycleOwner.set(view, lifecycleOwner)
        ViewTreeViewModelStoreOwner.set(view) { viewModelStore }
        view.setViewTreeSavedStateRegistryOwner(lifecycleOwner)

        return view
    }

    // Modified by ozobi - 2025/02/20 > 添加: 拖动隐藏
    @Composable
    private fun Content() {
        val context = LocalContext.current
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (mLayoutHierarchyView!!.mShowClickedNodeBounds) {
                        mLayoutHierarchyView!!.mClickedNodeInfo?.let {
                            val statusBarHeight = mLayoutHierarchyView!!.mStatusBarHeight
                            val rect = Rect(it.boundsInScreen)
                            rect.offset(0, -statusBarHeight)
                            drawRect(
                                color = Color(
                                    mLayoutHierarchyView!!.boundsPaint?.color ?: 0x2cd0d1
                                ),
                                topLeft = Offset(rect.left.toFloat(), rect.top.toFloat()),
                                size = Size(rect.width().toFloat(), rect.height().toFloat()),
                                style = Stroke(
                                    width = mLayoutHierarchyView!!.boundsPaint?.strokeWidth
                                        ?: 3f
                                )
                            )
                        }
                    }
                }
            }
            Column(modifier = Modifier.fillMaxSize()) {
                var isShowLayoutHierarchyView by remember {
                    mutableStateOf(true)
                }
                AndroidView(
                    factory = {
                        mLayoutHierarchyView!!
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    update = {
                        it.alpha = if (isShowLayoutHierarchyView) 1f else 0f
                    }
                )
                var offset by remember { mutableStateOf(Offset.Zero) }
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .offset {
                            IntOffset(offset.x.roundToInt(), offset.y.roundToInt())
                        }
                        .padding(0.dp,0.dp,5.dp,5.dp)
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .clickable {
                                close()
                            }
                    ) {
                        Text(
                            text = stringResource(R.string.text_exit_floating_window),
                            color=Color.White,
                            modifier = Modifier
                                .background(color=Color(0xddBA3636), shape = RoundedCornerShape(8.dp))
                                .padding(7.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .clickable {
                                expandAll()
                            }
                    ) {
                        Text(
                            text = stringResource(R.string.text_expand_all_hierarchy),
                            color=Color.White,
                            modifier = Modifier
                                .background(color=Color(0xdd53BA5C), shape = RoundedCornerShape(8.dp))
                                .padding(7.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .clickable {
                                showLayoutBounds()
                            }
                    ) {
                        Text(
                            text = stringResource(R.string.text_show_layout_bounds_window),
                            color=Color.White,
                            modifier = Modifier
                                .background(color=Color(0xdd7461BF), shape = RoundedCornerShape(8.dp))
                                .padding(7.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .background(color=Color(0xdd5AA6B5), shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp)
                            .clickable {
                                isShowLayoutHierarchyView = !isShowLayoutHierarchyView
                            }
                            .pointerInput(Unit){
                                detectDragGestures(
                                    onDragStart = {
                                        isShowLayoutHierarchyView = false
                                    },
                                    onDrag = { change, Offset ->
                                        change.consume()
                                        offset += Offset
                                    },
                                    onDragEnd = {
                                        offset = Offset(0f,0f)
                                        isShowLayoutHierarchyView = true
                                    },
                                    onDragCancel = {
                                        offset = Offset(0f,0f)
                                        isShowLayoutHierarchyView = true
                                    }
                                )
                            }
                    ) {
                        Text(
                            text = stringResource(R.string.text_hide_and_show),
                            color=Color.White,
                            modifier = Modifier
                                .padding(7.dp)
                        )
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(v: View) {
        // Modified by ozobi - 2025/02/19
        if(nightMode){
            mLayoutHierarchyView!!.setBackgroundColor(0xcc000000.toInt())
        }else{
            mLayoutHierarchyView!!.setBackgroundColor(COLOR_SHADOW.toInt())
        }
        // <
        mLayoutHierarchyView!!.setShowClickedNodeBounds(true)
        mLayoutHierarchyView!!.boundsPaint?.strokeWidth = 3f
        mLayoutHierarchyView!!.boundsPaint?.color = -0x2cd0d1
        // Added by ibozo - 2024/11/04 >
        mLayoutHierarchyView!!.setOnItemClickListener(object : OnItemClickListener,
            pl.openrnd.multilevellistview.OnItemClickListener {
                override fun onItemClicked(
                    parent: MultiLevelListView?,
                    view: View?,
                    item: Any?,
                    itemInfo: ItemInfo?
            ) {
                val nodeInfo = item as NodeInfo
                setSelectedNode(nodeInfo)
                if (view != null) {
                    mLayoutHierarchyView!!.ozobiSetSelectedNode(nodeInfo)
                }
            }

            override fun onGroupItemClicked(
                parent: MultiLevelListView?,
                view: View?,
                item: Any?,
                itemInfo: ItemInfo?
            ) {
                val nodeInfo = item as NodeInfo
                setSelectedNode(nodeInfo)
                if (view != null) {
                    mLayoutHierarchyView!!.ozobiSetSelectedNode(nodeInfo)
                }
            }
            override fun onItemClick(parent: RecyclerView?, item: View?, position: Int) {
                Log.d("ozobiLog","mLayoutHierarchyView: onItemClick")
            }
        })
        // <
        mLayoutHierarchyView!!.setOnItemLongClickListener { view: View, nodeInfo: NodeInfo ->
            mSelectedNode = nodeInfo
            ensureOperationPopMenu()
            if (mBubblePopMenu!!.contentView.measuredWidth <= 0) mBubblePopMenu!!.preMeasure()
            mBubblePopMenu!!.showAsDropDown(
                view,
                view.width / 2 - mBubblePopMenu!!.contentView.measuredWidth / 2,
                0
            )
        }
        mLayoutHierarchyView!!.setRootNode(mRootNode)
        mSelectedNode?.let { mLayoutHierarchyView!!.setSelectedNode(it) }
    }

    private fun ensureOperationPopMenu() {
        if (mBubblePopMenu != null) return
        mBubblePopMenu = BubblePopupMenu(
            mContext, listOf(
                mContext!!.getString(R.string.text_show_widget_infomation),
                mContext!!.getString(R.string.text_show_layout_bounds),
                mContext!!.getString(R.string.text_generate_code)
            )
        )
        mBubblePopMenu!!.setOnItemClickListener { view: View?, position: Int ->
            mBubblePopMenu!!.dismiss()
            when (position) {
                0 -> {
                    showNodeInfo()
                }
                1 -> {
                    showLayoutBounds()
                }
                else -> {
                    generateCode()
                }
            }
        }
        mBubblePopMenu!!.width = ViewGroup.LayoutParams.WRAP_CONTENT
        mBubblePopMenu!!.height = ViewGroup.LayoutParams.WRAP_CONTENT
    }

    private fun generateCode() {
        DialogUtils.showDialog(
            CodeGenerateDialog(mContext!!, mRootNode, mSelectedNode)
                .build()
        )
    }
    // Added by ibozo - 2024/11/04 >
    private fun expandAll() {
        mLayoutHierarchyView!!.expand()
    }
    // <
    private fun showLayoutBounds() {
        close()
        val window = LayoutBoundsFloatyWindow(mRootNode)
        window.setSelectedNode(mSelectedNode)
        FloatyService.addWindow(window)
    }

    fun showNodeInfo() {
        ensureNodeInfoDialog()
        mNodeInfoView!!.setNodeInfo(mSelectedNode!!)
        mNodeInfoDialog!!.show()
    }

    private fun ensureNodeInfoDialog() {
        var theme = Theme.LIGHT
        if(nightMode){
            theme = Theme.DARK
        }
        if (mNodeInfoDialog == null) {
            mNodeInfoView = NodeInfoView(mContext!!)
            mNodeInfoDialog = MaterialDialog.Builder(mContext!!)
                .customView(mNodeInfoView!!, false)
                .theme(theme)
                .build()
            mNodeInfoDialog!!.window?.setType(FloatyWindowManger.getWindowType())
        }
    }

    fun setSelectedNode(selectedNode: NodeInfo?) {
        mSelectedNode = selectedNode
    }

    companion object {
        private const val TAG = "FloatingHierarchyView"
        private const val COLOR_SHADOW = 0xccffffff// Modified by ozobi - 2025/02/19
    }
}