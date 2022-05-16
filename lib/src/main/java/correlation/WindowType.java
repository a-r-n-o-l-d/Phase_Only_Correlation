package correlation;

/*
 * Copyright (C) 2015 Arnold Fertin
 *
 * Centre National de la Recherche Scientifique
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */


/**
 *
 * @author Arnold Fertin
 */
public enum WindowType
{
    NONE,
    COSINE,
    HAMMING,
    HANN,
    TUKEY,
    WELCH;
    
    public static final String[] NAMES = new String[values().length];
    
    static
    {
        WindowType[] values = values();
        for (int i = 0; i < values.length; i++)
        {
            NAMES[i] = values[i].name();
        }
    }
}
