package yay.poloure.simplerss;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.format.Time;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

class parser
{
   static final SimpleDateFormat rss_date = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
   static final SimpleDateFormat rfc3339  = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");
   static final Pattern regex_tags        = Pattern.compile("(&lt;).*?(&gt;)");
   static final Pattern regex_cdata_tags  = Pattern.compile("\\<.*?\\>");
   static final Pattern space_tags        = Pattern.compile("[\\t\\n\\x0B\\f\\r\\|]");
   String[] start    = new String[]
   {
      "<link>", "<published>", "<pubDate>", "<description>", "<title","<content"
   };
   String[] end      = new String[]
   {
      "/link", "/publ", "/pubD", "/desc", "/titl", "/cont"
   };
   String[] of_types = new String[]
   {
      "<link>", "<published>", "<pubDate>", "<description>", "<title",
      "<content", "</link>", "</published>", "</pubDate>", "</description>",
      "</title", "</content", "<entry", "<item", "</entry", "</item"
   };
   String dump_path, url_path;
   int width;

   public parser(String group, String feed)
   {
      width = (int) Math.round(util.get_screen_width()*0.944);
      parse_local_xml(group, feed);
   }

   void parse_local_xml(String group, String feed)
   {
      String storage  = util.get_storage();
      String sep      = main.SEPAR;
      String g_dir    = main.GROUPS_DIR;
      String txt      = main.TXT;

      dump_path             = storage + "content.dump" + txt;
      url_path              = storage + "content.url"  + txt;
      String store_file     = storage + feed + main.STORE;
      String ex_feed_folder = storage + g_dir + group + sep + feed + sep;
      String content_file   = ex_feed_folder + feed + main.CONTENT;
      String image_dir      = ex_feed_folder + main.IMAGE_DIR;
      String thumbnail_dir  = ex_feed_folder + main.THUMBNAIL_DIR;
      String[] filters      = read.file(storage + main.FILTER_LIST);

      Set<String> set       = new LinkedHashSet<String>();
      Boolean write_mode    = false;
      Boolean c_mode        = false;
      Time time             = new Time();

      BufferedReader reader     = null;
      java.io.FileInputStream f = null;
      try
      {
         if(util.get_internal().equals(storage))
            reader = new BufferedReader(new FileReader(new File(store_file)));
         else
         {
            f      = util.get_context().openFileInput(util.create_internal_name(store_file));
            reader = new BufferedReader(new java.io.InputStreamReader(f, "UTF-8"));
         }
      }
      catch(FileNotFoundException t)
      {
         util.post("FileNotFoundException in parser 1.");
      }
      catch(IOException e)
      {
         util.post("IOException in parser 1.");
      }

      StringBuilder line    = new StringBuilder();
      String current_tag, temp_line, cont, image, image_name;
      int tem, tem2, tem3, description_length, take, cont_length, i;

      /* Read the file's lines to a set. */
      if(util.exists(content_file))
         set = read.set(content_file);

      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inJustDecodeBounds = true;

      int test = 0;
      while(test != -1)
      {
         try
         {
            reader.reset();
         }
         catch(IOException e)
         {
            util.post("Could not reset reader.");
         }

         try
         {
            current_tag = get_next_tag(reader, of_types);
         }
         catch(Exception e)
         {
            current_tag = "";
         }
         if((current_tag.contains("<entry"))||(current_tag.contains("<item")))
         {
            /// Add line to set and reset the line.
            if((line.length() > 1)&&(write_mode))
            {
               temp_line = line.toString();
               if(!temp_line.contains("published|")&&
                  !temp_line.contains("pubDate|")  &&
                  !set.contains(temp_line)           )
               {
                  temp_line = temp_line.concat(("pubDate|").concat(rfc3339.format(new Date()).concat("|")));
               }
               set.add(temp_line);
            }
            line.setLength(0);
            write_mode = true;
         }
         else if((current_tag.contains("</entry"))||(current_tag.contains("</item")))
         {
            image = check_for_image();
            util.post(image);
            if(!image.equals(""))
            {
               line.append("image|").append(image).append('|');
               image_name = image.substring(image.lastIndexOf(main.SEPAR) + 1, image.length());

               boolean success = false;
               /* If the image does not exist, try to download it from the internet. */
               if(!util.exists(image_dir + image_name))
                  success = write.dl(image, image_dir + image_name);

               /* If the image failed to download. */
               if(!success)
                  write.log("Failed to download image " + image);

               /* If the image downloaded fine and a thumbnail does not exist. */
               else if(!util.exists(thumbnail_dir + image_name))
                  compress_file(image_dir, thumbnail_dir, image_name);

               /* ISSUE #194 */
               BitmapFactory.decodeFile(thumbnail_dir + image_name, options);
               if(options.outWidth == 0)
                  write_mode = false;
               line.append("width|").append(options.outWidth).append('|')
                  .append("height|").append(options.outHeight).append('|');
            }
            line.append(check_for_url());
         }
         else
         {
            for(i = 0; i < start.length; i++)
            {
               if(current_tag.contains(start[i]))
               {
                  description_length = 0;

                  if(current_tag.contains("<title"))
                     current_tag = "<title>";
                  else if(current_tag.contains("<description"))
                  {
                     /// remove content
                     if(start.length == 6)
                     {
                        start    = util.remove_element(start, 5);
                        end      = util.remove_element(end, 5);
                        of_types = util.remove_element(of_types, 5);
                        of_types = util.remove_element(of_types, 10);
                     }
                     description_length = -2048;
                  }
                  else if(current_tag.contains("<content"))
                     current_tag = "<description>";

                  /// Write description| to the line buffer.
                  line.append(current_tag.substring(1, current_tag.length() - 1)).append("|");

                  cont = get_content_to_end_tag(reader, end[i]).trim();
                  cont_length = cont.length();

                  /// remove <![CDATA[ if it exists.
                  if((cont_length > 10)&&(cont.substring(0, 9).equals("<![CDATA[")))
                  {
                        cont = cont.substring(9, cont_length - 3);
                        c_mode = true;
                  }

                  cont = cont.replace("&amp;", "&").replace("&quot;", "\"");

                  /// Save the image url from cont.
                  if(current_tag.contains("<description"))
                  {
                     tem = cont.indexOf("img src=");
                     if(tem != -1)
                     {
                        tem2 = cont.indexOf("\"", tem + 10);
                        if(tem2 == -1)
                           tem2 = cont.indexOf("\'", tem + 10);
                        else
                        {
                           tem3 = cont.indexOf("\'", tem + 10);
                           if((tem3 != -1)&&(tem3 < tem2))
                                 tem2 = tem3;
                        }
                        write.single(dump_path, cont.substring(tem + 9, tem2) + main.NL);
                     }
                  }
                  /// If it follows the rss 2.0 specification for rfc882
                  else if(current_tag.equals("<pubDate>"))
                  {
                     try
                     {
                        cont = rfc3339.format(rss_date.parse(cont));
                     }
                     catch(Exception e)
                     {
                        write.log("BUG : Meant to be atom-3339 but looks like: " + cont);
                        cont = rfc3339.format(new Date());
                     }
                     line.append(cont).append("|");
                     break;
                  }
                  /// If it is an atom feed it will be one of four rfc3339 formats.
                  else if(current_tag.equals("<published>"))
                  {
                     try
                     {
                        time.parse3339(cont);
                        cont = time.format3339(false);
                     }
                     catch(Exception e)
                     {
                        write.log("BUG : Meant to be atom-3339 but looks like: " + cont);
                        cont = rfc3339.format(new Date());
                     }
                     line.append(cont).append("|");
                     break;
                  }

                  /// Replace all <x> with nothing.
                  if(c_mode)
                  {
                     cont = regex_cdata_tags.matcher(cont).replaceAll("");
                     c_mode = false;
                  }
                  else
                  {
                     cont = regex_tags.matcher(cont).replaceAll("");
                  }
                  cont = space_tags.matcher(cont).replaceAll(" ");

                  if(current_tag.contains("<title>"))
                  {
                     String cont2 = cont.toLowerCase();
                     for(String filter : filters)
                     {
                        if(cont2.contains(filter.toLowerCase()))
                        {
                           write_mode = false;
                           break;
                        }
                     }
                  }

                  take = description_length;
                  description_length += cont.length();

                  if((description_length > 512)&&(take < 512))
                     line.append(cont.substring(0, 512 - take));
                  else if(description_length < 512)
                     line.append(cont);

                  line.append("|");
                  break;
               }
            }
         }
         try
         {
            reader.mark(2);
            test = reader.read();
         }
         catch(IOException e)
         {
            util.post("IOException in testing next parser char.");
         }
      }

      /// Add the last line that has no <entry / <item after it.
      if(write_mode)
      {
         temp_line = line.toString();
         if(!temp_line.contains("published|")&&(!temp_line.contains("pubDate|"))&&(!set.contains(temp_line)))
            temp_line = temp_line.concat(("pubDate|").concat(rfc3339.format(new Date()).concat("|")));
         set.add(temp_line);
      }

      util.rm(store_file);
      /* Write the new content to the file. */
      write.collection(content_file, set);
   }

   String get_content_to_end_tag(BufferedReader reader, String tag)
   {
      /* </link> */
      StringBuilder cont = new StringBuilder();
      char[] buffer = new char[5];
      try
      {
         while(!(new String(buffer)).equals(tag))
         {
            cont.append(read_string_to_next_char(reader, '<'));
            /* hello my name is a penguin< */
            reader.mark(6);
            reader.read(buffer, 0, 5);
            reader.reset();
         }
         /* hello my name is a penguin<link>blash stha */
         cont.deleteCharAt(cont.length() - 1);
      }
      catch(Exception e)
      {
         return "";
      }
      return cont.toString();
   }

   String read_string_to_next_char(BufferedReader reader, char next)
   {
      char   current;
      char[] build = new char[4096];
      int    i     = 0;
      try
      {
         while((current = ((char) reader.read())) != next)
         {
            build[i] = current;
            i++;
         }
         build[i] = next;

         return new String(build, 0, i + 1);
      }
      catch(Exception e)
      {
         return "";
      }
   }

   String check_for_image()
   {
      String[] image_url = read.file(dump_path);
      if(image_url.length == 0)
         return "";

      if(image_url[0].length() <= 6)
         image_url[0] = "";

      util.rm(dump_path);
      return image_url[0];
   }

   String check_for_url()
   {
      String[] url = read.file(url_path);
      if(url.length == 0)
         return "";

      if(url[0].length() > 6)
         url[0] = "link|" + url + "|";
      else
         url[0] = "";

      util.rm(url_path);
      return url[0];
   }

   String get_next_tag(BufferedReader reader, String... types) throws IOException
   {
      boolean found = false;
      int tem, tem2, tem3;
      String tag = "";
      int eof;
      while(!found)
      {
         char current = '\0';
         while(current != '<')
         {
            eof = reader.read();
            if(eof == -1)
               return "eof";
            else
               current = (char) eof;
         }

         tag = "<" + read_string_to_next_char(reader, '>');

         tem = tag.indexOf("img src=");
         if(tem != -1)
         {
            tem2 = tag.indexOf("\"", tem + 10);
            if(tem2 == -1)
               tem2 = tag.indexOf("\'", tem + 10);
            else
            {
               tem3 = tag.indexOf("\'", tem + 10);
               if((tem3 != -1)&&(tem3 < tem2))
                     tem2 = tem3;
            }
            write.single(dump_path, tag.substring(tem + 9, tem2) + main.NL);
         }

         if((tag.contains("type=\"text/html"))||(tag.contains("type=\'text/html")))
         {
            tem = tag.indexOf("href=");
            if(tem != -1)
            {
               tem2 = tag.indexOf("\"", tem + 7);
               if(tem2 == -1)
                  tem2 = tag.indexOf("\'", tem + 7);
               else
               {
                  tem3 = tag.indexOf("\'", tem + 7);
                  if((tem3 != -1)&&(tem3 < tem2))
                        tem2 = tem3;
               }
               write.single(url_path, tag.substring(tem + 6, tem2) + main.NL);
            }
         }
         else if((tag.contains("type=\"image/jpeg"))||(tag.contains("type=\'image/jpeg")))
         {
            tem = tag.indexOf("href=");
            if(tem != -1)
            {
               tem2 = tag.indexOf("\"", tem + 7);
               if(tem2 == -1)
                  tem2 = tag.indexOf("\'", tem + 7);
               else
               {
                  tem3 = tag.indexOf("\'", tem + 7);
                  if((tem3 != -1)&&(tem3 < tem2))
                        tem2 = tem3;
               }
               util.rm(dump_path);
               write.single(dump_path, tag.substring(tem + 6, tem2) + main.NL);
            }
         }

         for(String type : types)
            if(tag.contains(type))
               found = true;
      }
      return tag;
   }

   void compress_file(String image_dir, String thumbnail_dir, String image_name)
   {

      BitmapFactory.Options o = new BitmapFactory.Options();
      o.inJustDecodeBounds = true;
      BitmapFactory.decodeFile(image_dir + image_name, o);

      float width_tmp = (float) o.outWidth;

      float insample = (width_tmp > width) ? (Math.round(width_tmp/width)) : 1;

      BitmapFactory.Options o2 = new BitmapFactory.Options();
      o2.inSampleSize = (int) insample;
      Bitmap bitmap = BitmapFactory.decodeFile(image_dir + image_name, o2);

      try
      {
         FileOutputStream out = new FileOutputStream(thumbnail_dir + image_name);
         bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
      }
      catch (Exception e)
      {
      }
   }
}
