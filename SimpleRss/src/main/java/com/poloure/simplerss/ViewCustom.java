package com.poloure.simplerss;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

class ViewCustom extends View
{
   static final int IMAGE_HEIGHT = 360;
   static final Paint[] PAINTS = new Paint[3];
   private static final int[] COLORS = {255, 165, 205};
   private static final float[] SIZES = {16.0F, 12.0F, 14.0F};

   static
   {
      for(int i = 0; 3 > i; i++)
      {
         PAINTS[i] = new Paint(Paint.ANTI_ALIAS_FLAG);
         PAINTS[i].setARGB(COLORS[i], 0, 0, 0);
      }
   }

   private Bitmap m_image;
   String m_title = "Initial Text";
   String m_link = "Initial Text";
   String m_linkFull = "Initial Text";
   String[] m_desLines = new String[3];
   private final int m_height;

   @Override
   public
   void onDraw(Canvas canvas)
   {
      float verticalPosition = drawBase(canvas);
      verticalPosition = drawBitmap(canvas, verticalPosition);
      if(null != m_desLines && 0 != m_desLines.length && null != m_desLines[0])
      {
         drawDes(canvas, verticalPosition);
      }
   }

   ViewCustom(Context context, int height)
   {
      super(context);
      m_height = height;

      Resources resources = context.getResources();
      DisplayMetrics metrics = resources.getDisplayMetrics();
      float eightFloatDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8.0F, metrics);
      int eightDp = Math.round(eightFloatDp);

      /* Set text sizes of the paints. */
      for(int i = 0; 3 > i; i++)
      {
         float size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, SIZES[i], metrics);
         PAINTS[i].setTextSize(size);
      }

      setBackgroundColor(Color.WHITE);
      setLayerType(LAYER_TYPE_HARDWARE, null);
      setPadding(eightDp, eightDp, eightDp, eightDp);
   }

   void setBitmap(Bitmap bitmap)
   {
      m_image = bitmap;
      if(null != bitmap)
      {
         invalidate();
      }
   }

   float drawBase(Canvas canvas)
   {
      /* Padding top. */
      float verticalPosition = getPaddingTop() + 20.0F;

      /* Draw the title. */
      canvas.drawText(m_title, getPaddingLeft(), verticalPosition, PAINTS[0]);
      verticalPosition += PAINTS[0].getTextSize();

      /* Draw the link. */
      canvas.drawText(m_link, getPaddingLeft(), verticalPosition, PAINTS[1]);
      return verticalPosition + PAINTS[1].getTextSize();
   }

   void drawDes(Canvas canvas, float verticalPosition)
   {
      for(int i = 0; 3 > i; i++)
      {
         canvas.drawText(m_desLines[i], getPaddingLeft(), verticalPosition, PAINTS[2]);
         verticalPosition += PAINTS[2].getTextSize();
      }
   }

   float drawBitmap(Canvas canvas, float verticalPosition)
   {
      if(null != m_image)
      {
         canvas.drawBitmap(m_image, 0.0F, verticalPosition, PAINTS[0]);
         return verticalPosition + IMAGE_HEIGHT + 32;
      }
      else
      {
         return verticalPosition;
      }
   }

   @Override
   protected
   void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
   {
      setMeasuredDimension(widthMeasureSpec, m_height);
   }
}
