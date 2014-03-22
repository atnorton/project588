class SessionsController < ApplicationController
  def new
  end

  def root
    if signed_in?
      redirect_to current_user
    else
      redirect_to :new_session
    end
  end

  def destroy
    log_out()
    redirect_to :new_session
  end

  def create
    @user = User.find_by(email: params[:session][:email].downcase)
    if(@user==nil)
      @user = User.new(email: params[:session][:email].downcase)
      @user.save
    end

    complete_token = SecureRandom.random_bytes(16)
    complete_token_s = Base64.urlsafe_encode64(complete_token)
    email_token = SecureRandom.random_bytes(16)
    email_token_s = Base64.urlsafe_encode64(email_token)

    user_token_s = Base64.urlsafe_encode64(complete_token.unpack('C*').zip(email_token.unpack('C*')).map{ |a,b| a ^ b }.pack('C*'))
    @user_token = user_token_s

    @session = Session.new(:user => @user, :session_key => Session.encrypt(user_token_s), :auth_token => complete_token_s)
    @session.save
    cookies.permanent[:session_id] = @user_token
    
    AuthMailer.auth_email(params[:session][:email].downcase, email_token_s).deliver

    @email_token = email_token_s
  end

  def waitForLogin
    render :json => { :result => signed_in? }
  end

  def index
  end

  def authenticate
    user_token = cookies.permanent[:session_id]
    if request.post?
      email_token = params[:login][:email_token]
    else
      email_token = params[:email_token]
    end

    if log_in(user_token, email_token)
      redirect_to current_user 
    else
      redirect_to :new_session
    end
  end
end
