/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.poloure.simplerss;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public
class FeedItem implements Serializable
{
    private static final long serialVersionUID = 3L;

    public String m_title = "";
    public String m_imageLink = "";
    public String m_imageName = "";
    public String m_urlTrimmed = "";
    public String m_url = "";
    public String m_content = "";
    public String[] m_desLines = {"", "", ""};
    public Long m_time = 0L;

    private
    void writeObject(ObjectOutputStream out) throws IOException
    {
        out.writeUTF(m_title);
        out.writeUTF(m_imageLink);
        out.writeUTF(m_imageName);
        out.writeUTF(m_urlTrimmed);
        out.writeUTF(m_url);
        out.writeUTF(m_content);
        out.writeObject(m_desLines);
        out.writeLong(m_time);
    }

    private
    void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        m_title = in.readUTF();
        m_imageLink = in.readUTF();
        m_imageName = in.readUTF();
        m_urlTrimmed = in.readUTF();
        m_url = in.readUTF();
        m_content = in.readUTF();
        m_desLines = (String[]) in.readObject();
        m_time = in.readLong();
    }
}
