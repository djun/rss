package yay.poloure.simplerss;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.Set;
import java.util.regex.Pattern;

class adapter_feeds_cards extends BaseAdapter
{
   String[] links        = new String[0];
   String[] titles       = new String[0];
   String[] descriptions = new String[0];
   String[] images       = new String[0];
   Integer[] heights     = new Integer[0];
   Integer[] widths      = new Integer[0];

   static Set<String> read_items = read.set(main.storage + main.READ_ITEMS);

   static final Pattern thumb_img = Pattern.compile("thumbnails");
   static Context context;
   static LayoutInflater inflater;
   static int two = 0, four = 0, eight = 0;
   static int screen_width;

   static final int link_black  = Color.rgb(128, 128, 128);
   static final int link_grey   = Color.rgb(194, 194, 194);

   static final int des_black   = Color.rgb(78, 78, 78);
   static final int des_grey    = Color.rgb(167, 167, 167);

   static final int title_black = Color.rgb(0, 0, 0);
   static final int title_grey  = Color.rgb(128, 128, 128);

   boolean   first          = true;
   ListView  listview;
   boolean   touched        = true;

   public adapter_feeds_cards(Context context_main)
   {
      if(context == null)
      {
         context                 = context_main;
         DisplayMetrics metrics  = context.getResources().getDisplayMetrics();
         inflater                = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         screen_width            = util.get_screen_width();
         if(two == 0)
         {
            two      = (int) ((2  * (metrics.density) + 0.5f));
            four     = (int) ((4  * (metrics.density) + 0.5f));
            eight    = (int) ((8  * (metrics.density) + 0.5f));
         }
      }
   }

   void add_array(String[] new_title, String[] new_des, String[] new_link, String[] new_image, Integer[] new_height, Integer[] new_width)
   {
      titles         = util.concat(titles,       new_title);
      descriptions   = util.concat(descriptions, new_des);
      links          = util.concat(links,        new_link);
      images         = util.concat(images,       new_image);
      heights        = util.concat(heights,      new_height);
      widths         = util.concat(widths,       new_width);
   }

   @Override
   public int getCount()
   {
      return titles.length;
   }

   @Override
   public long getItemId(int position)
   {
      return position;
   }

   @Override
   public String getItem(int position)
   {
      return titles[position];
   }

   @Override
   public int getViewTypeCount()
   {
      return 4;
   }

   @Override
   public int getItemViewType(int position)
   {
      boolean img = ( widths[position] != null &&
                      widths[position] != 0 );
      boolean des = ( descriptions[position] != null &&
                     !descriptions[position].equals("") );

      if(img && des)
         return 0;
      if(img && !des)
         return 1;
      if(!img && des)
         return 2;
      if(!img && !des)
         return 3;

      return 3;
   }

   /* If the listview starts at the very top of a list with 20 items, position 19 is the only on calling getView(). */
   @Override
   public View getView(int position, View convertView, ViewGroup parent)
   {
      int view_type = getItemViewType(position);

      if(first)
      {
         listview = (ListView) parent;
         listview.setScrollingCacheEnabled(false);
         listview.setOnScrollListener(new AbsListView.OnScrollListener()
         {
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount){
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState)
            {
               /* The very top item is read only when the padding exists above it. */
               /* links.get(0) == the last link in the list. position is always 76*/
               if(listview.getChildAt(0).getTop() == eight && listview.getVisibility() == View.VISIBLE && touched)
                  read_items.add(links[links.length - 1]);

               if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE)
                  update.navigation(null);
            }
         });
      }

      /* card_full.xml img && des. */
      if(view_type == 0)
      {
         full_holder holder;
         if(convertView == null)
         {
            convertView  = inflater.inflate(R.layout.card_full, parent, false);
            holder       = new full_holder();
            holder.title = (TextView)  convertView.findViewById(R.id.title);
            holder.url   = (TextView)  convertView.findViewById(R.id.url);
            holder.des   = (TextView)  convertView.findViewById(R.id.description);
            holder.image = (ImageView) convertView.findViewById(R.id.image);
            convertView.setOnClickListener(new webview_mode());
            convertView.setOnLongClickListener(new long_press());
            convertView.setTag(holder);
         }
         else
            holder = (full_holder) convertView.getTag();

         display_img(holder.image, position);

         holder.title.setText(titles[position]);
         holder.des  .setText(descriptions[position]);
         holder.url  .setText(links[position]);
      }
      /* card_no_des_img.xml no description, image, title. */
      else if(view_type == 1)
      {
         img_no_des_holder holder;
         if(convertView == null)
         {
            convertView  = inflater.inflate(R.layout.card_no_des_img, parent, false);
            holder       = new img_no_des_holder();
            holder.title = (TextView)  convertView.findViewById(R.id.title);
            holder.url   = (TextView)  convertView.findViewById(R.id.url);
            holder.image = (ImageView) convertView.findViewById(R.id.image);
            convertView.setOnClickListener(new webview_mode());
            convertView.setOnLongClickListener(new long_press());
            convertView.setTag(holder);
         }
         else
            holder = (img_no_des_holder) convertView.getTag();

         display_img(holder.image, position);

         holder.title.setText(titles[position]);
         holder.url  .setText(links[position]);
      }
      /* card_des_no_img.xml no image, descirition, title. */
      else if(view_type == 2)
      {
         no_img_des_holder holder;
         if(convertView == null)
         {
            convertView  = inflater.inflate(R.layout.card_des_no_img, parent, false);
            holder       = new no_img_des_holder();
            holder.title = (TextView)  convertView.findViewById(R.id.title);
            holder.url   = (TextView)  convertView.findViewById(R.id.url);
            holder.des   = (TextView)  convertView.findViewById(R.id.description);
            convertView.setOnClickListener(new webview_mode());
            convertView.setOnLongClickListener(new long_press());
            convertView.setTag(holder);
         }
         else
            holder = (no_img_des_holder) convertView.getTag();

         holder.title.setText(titles[position]);
         holder.des  .setText(descriptions[position]);
         holder.url  .setText(links[position]);
      }
      /* No description or image. */
      else if(view_type == 3)
      {
         no_img_no_des_holder holder;
         if(convertView == null)
         {
            convertView  = inflater.inflate(R.layout.card_no_des_no_img, parent, false);
            holder       = new no_img_no_des_holder();
            holder.title = (TextView)  convertView.findViewById(R.id.title);
            holder.url   = (TextView)  convertView.findViewById(R.id.url);
            convertView.setOnClickListener(new webview_mode());
            convertView.setOnLongClickListener(new long_press());
            convertView.setTag(holder);
         }
         else
            holder = (no_img_no_des_holder) convertView.getTag();

         holder.title.setText(titles[position]);
         holder.url  .setText(links[position]);
      }

      /* Stuff unrelated to the view creation below here. */

      /* Alpha MIN API 11 - Also may be a performance hog - If the item is read, grey it out
       * int colour = holder.time_view.getCurrentTextColor();*/
     /* if(main.HONEYCOMB)
      {
         if(read_items.contains(links[position]))
         {
            holder.title_view       .setTextColor(title_grey);
            holder.description_view .setTextColor(des_grey);
            holder.time_view        .setTextColor(link_grey);
            if(image_exists)
               holder.image_view.setAlpha(0.5f);
         }
         else
         {
            holder.title_view       .setTextColor(title_black);
            holder.description_view .setTextColor(des_black);
            holder.time_view        .setTextColor(link_black);
            if(image_exists)
               holder.image_view.setAlpha(1.0f);
         }
      }*/

      /* The logic that tells whether the item is read or not. */
      if(listview.getVisibility() == View.VISIBLE && position - 1 >= 0 && touched)
         read_items.add(links[position - 1]);

      return convertView;
   }

   void display_img(ImageView view, int position)
   {
      int height  = heights[position];
      int width   = widths[position];

      view.setImageDrawable(new ColorDrawable(Color.WHITE));
      ViewGroup.LayoutParams iv = view.getLayoutParams();

      iv.height = (int) ((((double) screen_width)/(width)) * (height));
      view.setLayoutParams(iv);
      view.setTag(position);

      if(!main.HONEYCOMB)
         (new image()).execute(view, view.getTag());
      else
         (new image()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, view, view.getTag());
   }

   class image extends AsyncTask<Object, Void, Object[]>
   {
      ImageView iv;
      int tag;

      @Override
      protected Object[] doInBackground(Object... params)
      {
         iv    = (ImageView)  params[0];
         tag   = (Integer)    params[1];
         BitmapFactory.Options o = new BitmapFactory.Options();
         o.inSampleSize = 1;
         Animation fadeIn = new AlphaAnimation(0, 1);
         fadeIn.setDuration(240);
         fadeIn.setInterpolator(new DecelerateInterpolator());
         iv.setOnClickListener(new image_call(thumb_img.matcher(images[tag]).replaceAll("images")));
         Object[] ob = {BitmapFactory.decodeFile(images[tag], o), fadeIn};
         return ob;
      }

      @Override
      protected void onPostExecute(Object... result)
      {
         if((Integer) iv.getTag() != tag)
            return;
         if(iv != null && result[0] != null)
         {
            iv.setImageBitmap((Bitmap) result[0]);
            iv.startAnimation((Animation) result[1]);
            if((Integer) iv.getTag() != tag)
               return;
            iv.setVisibility(View.VISIBLE);
         }
      }
   }


   static class full_holder
   {
      TextView title;
      TextView url;
      TextView des;
      ImageView image;
   }

   static class no_img_des_holder
   {
      TextView title;
      TextView url;
      TextView des;
   }

   static class img_no_des_holder
   {
      TextView title;
      TextView url;
      ImageView image;
   }

   static class no_img_no_des_holder
   {
      TextView title;
      TextView url;
   }

   class webview_mode implements View.OnClickListener
   {
      @Override
      public void onClick(View v)
      {
         main.action_bar.setTitle("Offline");
         navigation_drawer.drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
         navigation_drawer.drawer_toggle.setDrawerIndicatorEnabled(false);
         main.action_bar.setDisplayHomeAsUpEnabled(true);
         main.fman.beginTransaction()
               .hide(main.fman.findFragmentByTag(navigation_drawer.NAV_TITLES[0]))
               .add(R.id.drawer_layout, new fragment_webview(), "OFFLINE")
               .addToBackStack("BACK")
               .commit();
      }
   }

   class image_call implements View.OnClickListener
   {
      private final String image_path;
      public image_call(String im)
      {
         image_path = im;
      }

      @Override
      public void onClick(View v)
      {
         Intent intent = new Intent();
         intent.setAction(Intent.ACTION_VIEW);
         String type = image_path.substring(image_path.lastIndexOf('.') + 1, image_path.length());

         if(!main.JELLYBEAN)
            intent.setDataAndType(Uri.fromFile(new File(image_path)), "image/" + type);
         else
            intent.setDataAndTypeAndNormalize(Uri.fromFile(new File(image_path)), "image/" + type);

         context.startActivity(intent);
      }
   }

   class long_press implements View.OnLongClickListener
   {
      @Override
      public boolean onLongClick(View v)
      {
         String long_press_url = util.getstr((TextView) v.findViewById(R.id.url));
        /* show_card_dialog(context, long_press_url, ((ViewHolder) v.getTag()).image_view.getVisibility());*/
         return true;
      }
   }

   static void show_card_dialog(final Context con, final String URL, final int image_visibility)
   {
      String[] menu_items;
      if(image_visibility != View.VISIBLE)
         menu_items = util.get_array(con, R.array.card_menu);
      else
         menu_items = util.get_array(con, R.array.card_menu_image);


      final AlertDialog card_dialog = new AlertDialog.Builder(con)
            .setCancelable(true)
            .setItems(menu_items, new DialogInterface.OnClickListener()
            {
               @Override
               public void onClick(DialogInterface dialog, int position)
               {
                  switch(position)
                  {
                     case(0):
                        ClipboardManager clipboard = (ClipboardManager) con.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("label", URL);
                        clipboard.setPrimaryClip(clip);
                        break;
                     case(1):
                        con.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(URL)));
                     /*case(2):
                        break;*/
                  }
               }
            })
            .create();

            card_dialog.show();
   }

   class fragment_webview extends Fragment
   {
      WebView web_view;
      FrameLayout view;
      TextView text;

      public fragment_webview()
      {
      }

      @Override
      public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
      {
         if(web_view != null)
            web_view.destroy();

         view = new FrameLayout(getActivity());
         web_view = new WebView(getActivity());
         view.addView(web_view, LayoutParams.MATCH_PARENT);

         /*text = new TextView(getActivity());
         text.setText("webview");
         text.setGravity(Gravity.CENTER);
         text.setVisibility(View.GONE);
         view.addView(text, android.widget.FrameLayout.LayoutParams.WRAP_CONTENT);*/

         return view;
      }

      @Override
      public void onPause()
      {
      /* min api 11. */
         web_view.onPause();
         super.onPause();
      }

      @Override
      public void onResume()
      {
         /* min api 11. */
         web_view.onResume();
         super.onResume();
      }

      @Override
      public void onDestroy()
      {
         if(web_view != null)
         {
            view.removeAllViews();
            web_view.removeAllViews();
            web_view.destroy();
            web_view = null;
            view = null;
         }
         super.onDestroy();
      }

      WebView get_webview()
      {
         return web_view;
      }
   }
}
