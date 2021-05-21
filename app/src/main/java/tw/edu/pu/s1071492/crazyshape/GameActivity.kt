package tw.edu.pu.s1071492.crazyshape

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.activity_main.*
import org.tensorflow.lite.support.image.TensorImage
import tw.edu.pu.s1071492.crazyshape.ml.Shapes


class GameActivity : AppCompatActivity() {
    var back = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        intent = getIntent()
        var type = intent.getStringExtra("形狀")
        var msg :TextView = findViewById(R.id.txvMsg)
        if(type == "circle"){
            msg.setText("請畫出圓形")
        }
        else if(type == "triangle"){
            msg.setText("請畫出三角形")
        }
        else if(type == "star"){
            msg.setText("請畫出星形")
        }
        else if(type == "square"){
            msg.setText("請畫出方形")
        }
        btn.setOnClickListener(object: View.OnClickListener{
            override fun onClick(p0: View?) {
                handv.path.reset()
                handv.invalidate()
            }
        })
        var lock :Button= findViewById(R.id.btnpre)

        btnpre.setOnClickListener(object:View.OnClickListener{
            override fun onClick(p0: View?) {
                finish()
            }
        })

        handv.setOnTouchListener(object:View.OnTouchListener{
            override fun onTouch(p0: View?, event: MotionEvent): Boolean {
                var xPos = event.getX()
                var yPos = event.getY()
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> handv.path.moveTo(xPos, yPos)
                    MotionEvent.ACTION_MOVE -> handv.path.lineTo(xPos, yPos)
                    MotionEvent.ACTION_UP -> {
                        //將handv轉成Bitmap
                        val b = Bitmap.createBitmap(handv.measuredWidth, handv.measuredHeight,
                            Bitmap.Config.ARGB_8888)
                        val c = Canvas(b)
                        handv.draw(c)
                        classifyDrawing(b)
                    }
                }
                handv.invalidate()
                return true
            }
        })
    }
    fun classifyDrawing(bitmap : Bitmap) {
        val model = Shapes.newInstance(this)
        // Creates inputs for reference.
        val image = TensorImage.fromBitmap(bitmap)

        // Runs model inference and gets result.
        val outputs = model.process(image)
            .probabilityAsCategoryList.apply {
                sortByDescending { it.score } // 排序，高匹配率優先
            }.take(1)  //取最高的1個


        var Result:String = ""
        when (outputs[0].label) {
            "circle" -> Result = "圓形"
            "square" -> Result = "方形"
            "star" -> Result = "星形"
            "triangle" -> Result = "三角形"
        }
        Result += ": " + String.format("%.1f%%", outputs[0].score * 100.0f)



        // Releases model resources if no longer used.
        model.close()
        Toast.makeText(this, Result, Toast.LENGTH_SHORT).show()
    }



}