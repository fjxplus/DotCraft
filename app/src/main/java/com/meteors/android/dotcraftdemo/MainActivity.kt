package com.meteors.android.dotcraftdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.view.*
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meteors.android.dotcraftdemo.databinding.ActivityMainBinding
import com.meteors.android.dotcraftdemo.databinding.DotItemBinding

private const val TAG = "DotCraft"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding       //activity_main的ViewBinding

    private val dotsList = ArrayList<List<Dot>>()       //用于存储三个Dot集合，每个Dot集合保存若干个黑白Dot

    private val ringPosition = ArrayList<Int>()         //保存ring的随机位置

    private val dotPosition = ArrayList<Int>()          //保存dot的随机位置

    private var degree = 3          //规模3x3

    private var startTime: Long = 0     //保存游戏起始时间

    private var endTime: Long = 0       //保存完成时间

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initDots()          //初始化Dot位置
        initRings()         //初始化Ring位置

        //timer为Chronometer计时器组件，设置基准，开始计时
        binding.timer.apply {
            startTime = SystemClock.elapsedRealtime()
            base = startTime
            start()
        }

        //submit为完成按钮，判断关灯成功逻辑，成功的话计算总时间
        binding.submit.setOnClickListener {
            if (judge()) {
                endTime = binding.timer.drawingTime
                binding.timer.stop()
                val totalTime = (endTime - startTime) / 1000.0
                binding.submit.visibility = View.INVISIBLE
                Toast.makeText(this, "总用时 $totalTime 秒", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "未成功", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 1. 初始化 Dot：为每一列取随机数，获取dot的随机位置，并保存
     * 2. 配置 RecyclerView的Adapter和LayoutManager,添加滑动监听
     */
    private fun initDots() {
        dotsList.clear()
        dotPosition.clear()

        for (i in 0 until degree) {             //[0,3)循环三次
            val dots = ArrayList<Dot>()         //创建保存dot的列表
            val dotLocation =
                (0 + Math.random() * ((degree - 1) - 0 + 1)).toInt()      //取[0,2]的随机数，即白色dot的位置
            dotPosition.add(dotLocation)
            for (j in 0 until degree) {         //创建Dot实例，随机数位置创建白色Dot
                val dot = if (j == dotLocation) {
                    Dot(DOT_WHITE)
                } else {
                    Dot(DOT_BLACK)
                }
                dots.add(dot)
            }
            dots.apply {                //扩充RecyclerView的item容量，允许上下滑动
                addAll(this)
                addAll(this)
                addAll(this)
            }
            dotsList.add(dots)
        }

        //配置RecyclerView，定位到中间位置，设置滑动监听，保持每个界面显示完整的三个item，位置固定
        val dotContainer = listOf<RecyclerView>(
            binding.dotContainerRow0,
            binding.dotContainerRow1,
            binding.dotContainerRow2
        )
        for (i in 0 until degree) {
            dotContainer[i].apply {
                layoutManager = LinearLayoutManager(this@MainActivity)
                adapter = DotAdapter(dotsList[i])
                scrollToPosition(dotsList[i].size / 2)
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                        val firstPosition = layoutManager.findFirstVisibleItemPosition()
                        val lastPosition = layoutManager.findLastVisibleItemPosition()
                        val firstP = firstPosition % 3
                        val lastP = lastPosition % 3
                        if (firstP == lastP) {      //表示页面中出现四个item
                            recyclerView.smoothScrollToPosition(lastPosition)
                        }
                    }
                })
            }
        }

    }


    /**
     * 初始化Ring的位置，让所有Ring皆不可见，根据取得的随机数决定可见Ring的位置
     */
    private fun initRings() {
        ringPosition.clear()
        val ringList = listOf(
            binding.ring0,
            binding.ring1,
            binding.ring2,
            binding.ring3,
            binding.ring4,
            binding.ring5,
            binding.ring6,
            binding.ring7,
            binding.ring8
        )
        for (element in ringList) {
            element.visibility = View.INVISIBLE
        }
        for (i in 0 until degree) {
            val ringLocation = (0 + Math.random() * ((degree - 1) - 0 + 1)).toInt()
            ringPosition.add(ringLocation)
            ringList[ringLocation * degree + i].visibility = View.VISIBLE
        }

    }

    /**
     * 菜单栏的重新开始按钮，对Dot、Ring、计数器进行初始化
     */
    private fun restart() {
        initDots()
        initRings()
        binding.timer.apply {
            startTime = SystemClock.elapsedRealtime()
            base = startTime
            start()
        }
        binding.submit.visibility = View.VISIBLE
    }

    /**
     * 判断ring和dot是否对应
     */
    private fun judge(): Boolean {
        //完成判断逻辑
        return true
    }

    //加载菜单项
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    //为菜单项设置事件监听
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.restart -> {
                restart()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //
    private class DotHolder(itemBinding: DotItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        private val dot = itemBinding.dot

        //根据dot中保存的颜色设置图片资源
        fun setColor(color: Int) {
            when (color) {
                DOT_WHITE -> dot.setImageResource(R.drawable.shape_dot_white)
                DOT_BLACK -> dot.setImageResource(R.drawable.shape_dot_black)
            }
        }
    }

    private inner class DotAdapter(val dots: List<Dot>) : RecyclerView.Adapter<DotHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DotHolder {
            val itemBinding =
                DotItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return DotHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: DotHolder, position: Int) {
            val dot = dots[position]
            holder.setColor(dot.color)
        }

        override fun getItemCount(): Int {
            return dots.size        //Item数量
        }
    }
}