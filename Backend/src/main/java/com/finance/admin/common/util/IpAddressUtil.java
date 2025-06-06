package com.finance.admin.common.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

public class IpAddressUtil {

    private static final String[] IP_HEADER_CANDIDATES = {
        "X-Forwarded-For",
        "Proxy-Client-IP",
        "WL-Proxy-Client-IP",
        "HTTP_X_FORWARDED_FOR",
        "HTTP_X_FORWARDED",
        "HTTP_X_CLUSTER_CLIENT_IP",
        "HTTP_CLIENT_IP",
        "HTTP_FORWARDED_FOR",
        "HTTP_FORWARDED",
        "HTTP_VIA",
        "REMOTE_ADDR"
    };

    public static String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        for (String header : IP_HEADER_CANDIDATES) {
            String ipList = request.getHeader(header);
            if (StringUtils.hasText(ipList) && !"unknown".equalsIgnoreCase(ipList)) {
                // Handle comma-separated list of IPs (e.g., from X-Forwarded-For)
                String[] ips = ipList.split(",");
                for (String ip : ips) {
                    ip = ip.trim();
                    if (isValidIpAddress(ip)) {
                        return ip;
                    }
                }
            }
        }

        // Fallback to remote address
        String remoteAddr = request.getRemoteAddr();
        return isValidIpAddress(remoteAddr) ? remoteAddr : "unknown";
    }

    private static boolean isValidIpAddress(String ip) {
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            return false;
        }

        // Basic validation for IPv4 and IPv6
        return ip.matches("^([0-9]{1,3}\\.){3}[0-9]{1,3}$") || // IPv4
               ip.matches("^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$") || // IPv6 full
               ip.matches("^::1$") || // IPv6 localhost
               ip.equals("127.0.0.1"); // IPv4 localhost
    }

    public static boolean isLocalhost(String ip) {
        return "127.0.0.1".equals(ip) || "::1".equals(ip) || "localhost".equals(ip);
    }

    public static boolean isPrivateNetworkIp(String ip) {
        if (!StringUtils.hasText(ip)) {
            return false;
        }

        // Check for private IPv4 ranges
        if (ip.startsWith("192.168.") || 
            ip.startsWith("10.") || 
            (ip.startsWith("172.") && isInRange172(ip))) {
            return true;
        }

        // Check for IPv6 private ranges
        return ip.startsWith("fc00:") || ip.startsWith("fd00:");
    }

    private static boolean isInRange172(String ip) {
        try {
            String[] parts = ip.split("\\.");
            if (parts.length >= 2) {
                int secondOctet = Integer.parseInt(parts[1]);
                return secondOctet >= 16 && secondOctet <= 31;
            }
        } catch (NumberFormatException e) {
            // Invalid IP format
        }
        return false;
    }
} 
