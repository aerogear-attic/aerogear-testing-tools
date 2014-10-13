package org.arquillian.spacelift.gradle.utils

import org.apache.commons.lang3.SystemUtils

/**
 * Utility methods for determining user or OS these scripts runs at.
 *
 */
class EnvironmentUtils {

    static def runsOnWindows() {
        SystemUtils.IS_OS_WINDOWS
    }

    static def runsOnLinux() {
        SystemUtils.IS_OS_LINUX
    }
    
    static runsOnHudson() {
        System.getProperty("user.name") ==~ /hudson/
    }

    static enum IP {
        v6, v4
    }

    /**
    * The map of loopback ip adresses for IPv4/v6
    */
    static def lo = [(IP.v6):"::1", (IP.v4):"127.0.0.1"]

    /**
    * The map of all the environments to search for IPv4/v6 adresses
    */
    static def ip_envs = [
       (IP.v4):["MYTESTIP_1","MYTESTIP_2"],
       (IP.v6):["MYTESTIPV6_1","MYTESTIPV6_2"]
    ];


    /**
    * Get a bindable address for IPv4/v6.
    * First, try searching environment variables in ``ip_envs``, 
    * then try if at least loopback is working,
    * if there is no relevant env-var set, and lo is inot working, return null
    */
    static def getIp = {IP v ->
        def preset_ips = ip_envs[v].collect({System.getenv(it)}).findAll()
        if(preset_ips){
            return preset_ips.first()
        }else if(InetAddress.getByName(lo[v]).isReachable(3000)){
            return lo[v]
        }else{
            return null
        }
    }

    /**
    * Returns the list of first adress for IPv6 and IPv4
    * It will return either both IPv4 and IPv6 adresses
    * or either of those, or none, id none works
    */
    static def ipVersionsToTest() {
        [IP.v4,IP.v6].collect(getIp).findAll()
    }   
}
