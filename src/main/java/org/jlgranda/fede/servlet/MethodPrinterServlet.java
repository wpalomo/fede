/*
 * Copyright 2013 jlgranda.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jlgranda.fede.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author jlgranda
 */
public class MethodPrinterServlet extends HttpServlet {

    private static final int BYTES_DOWNLOAD = 1024;
    private static final long serialVersionUID = 2473911299579887520L;

    @Override
    public void doGet(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        response.setContentType("image/png");

        String methodId = request.getParameter("methodId");//download or display
	/*response.setHeader("Content-Disposition",
         "attachment;filename=downloadname.txt");*/
        ServletContext ctx = getServletContext();
        InputStream is = ctx.getResourceAsStream("/method/" + methodId + ".png");

        int read = 0;
        byte[] bytes = new byte[BYTES_DOWNLOAD];
        OutputStream os = response.getOutputStream();

        while ((read = is.read(bytes)) != -1) {
            os.write(bytes, 0, read);
        }
        os.flush();
        os.close();
    }
}
