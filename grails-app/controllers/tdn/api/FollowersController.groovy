package tdn.api

import com.tdnsecuredrest.User
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.grails.web.json.JSONArray

import javax.annotation.security.RolesAllowed


class FollowersController {

    transient springSecurityService
    static transients = ['springSecurityService']

    def index(Long offset, Long max) {
        List users = User.executeQuery("select u.followers from User u where u.id = ?",
                [springSecurityService.principal.id], [max: max, offset: offset])
        List<User> following = User.executeQuery("from User as u where :user in elements(u.followers)",
                [user: User.get(springSecurityService.principal.id)])
        JSONArray arr = new JSONArray();
        users.forEach {
            u -> def json = JSON.parse((u as JSON).toString())
                json.put("isFollowing", following.contains(u))
                arr.put(json)
        }
        render arr as JSON
    }

    def followerCount(Long id) {
        def followerCount = [followerCount: User.get(id).followers.size()]
        render followerCount as JSON
    }

    def save(Long id) {
        User u = User.get(id)
        u.addToFollowers(User.get(springSecurityService.principal.id))
        u.save(flush: true, failOnError: true)
        render u.followers as JSON
    }

    def delete(Long id) {
        User u = User.get(id)
        u.removeFromFollowers(User.get(springSecurityService.principal.id))
        u.save(flush: true, failOnError: true)
        render u.followers as JSON
    }

    def following(Long id, Long offset, Long max) {
        List user = User.executeQuery("from User as u where :user in elements(u.followers)", [user: User.get(id)], [offset: offset, max: max])
        render user as JSON
    }

    def followingCount(Long id) {
        def followingCount = [followingCount: User.executeQuery("from User as u where :user in elements(u.followers)", [user: User.get(id)], [offset: offset, max: max]).size()]
        render followingCount as JSON
    }
}
