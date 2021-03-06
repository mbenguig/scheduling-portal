/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive_grid_cloud_portal.scheduler.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ow2.proactive_grid_cloud_portal.common.server.Service;
import org.ow2.proactive_grid_cloud_portal.common.shared.RestServerException;
import org.ow2.proactive_grid_cloud_portal.common.shared.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The servlet which is called when the XML of a job is to be downloaded
 * @author ahagea
 *
 */
@SuppressWarnings("serial")
public class DownloadJobXMLServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadJobXMLServlet.class);

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        getJobXML(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        getJobXML(request, response);
    }

    private void getJobXML(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String jobId = request.getParameter("jobId");
        String sessionId = request.getParameter("sessionId");
        String fileName = "job_" + jobId + ".xml";

        try (ServletOutputStream out = response.getOutputStream();
                InputStream is = new ByteArrayInputStream(((SchedulerServiceImpl) Service.get()).getJobXML(sessionId,
                                                                                                           jobId)
                                                                                                .getBytes(StandardCharsets.UTF_8))) {
            response.setContentType("application/xml");
            response.setHeader("Content-disposition", "attachment; filename=" + fileName);
            response.setHeader("Location", fileName);

            int buffer;
            while ((buffer = is.read()) != -1) {
                out.write(buffer);
            }
            out.flush();
            LOGGER.debug("Successfully downloaded job XML for job: {}", jobId);

        } catch (RestServerException e) {
            if (e.getStatus() == HttpServletResponse.SC_FORBIDDEN) {
                LOGGER.debug("Failed to download workflow. User permission not granted: ", e);
                response.addHeader("proactive_error", "User permission not granted: HTTP status code " + e.getStatus());
            } else {
                LOGGER.warn("Failed to download workflow", e);
            }
            response.sendError(e.getStatus(), e.getMessage());
        } catch (IOException | ServiceException e) {
            LOGGER.warn("Failed to download workflow", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                               "Failed to download workflow: " + e.getMessage());
        }
    }
}
