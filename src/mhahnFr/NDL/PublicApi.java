/*
 * NDL4Java - Native Dark mode Listener for Java.
 *
 * Copyright (C) 2024  mhahnFr
 *
 * This file is part of NDL4Java.
 *
 * NDL4Java is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NDL4Java is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with NDL4Java.  If not, see <https://www.gnu.org/licenses/>.
 */

package mhahnFr.NDL;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation marks symbols that are exposed as public API.
 *
 * @author mhahnFr
 * @since 03.06.24
 */
@Retention(RetentionPolicy.SOURCE)
public @interface PublicApi {}
