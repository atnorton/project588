class SessionsController < ApplicationController
  protect_from_forgery :except => [:authenticate]

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
    log_out
    redirect_to :new_session
  end

  def create
    @user = User.find_by(email: params[:session][:email].downcase)
    if(@user==nil)
      @user = User.new(email: params[:session][:email].downcase)
      @user.save
    end

    @user_token, @email_token, complete_token = EmailAuth.generateTokens(32)

    session_id = Session.generateSessionId

    @session = Session.new(:user => @user, :session_id => Session.encrypt(session_id), :session_key => Session.encrypt(@user_token), :auth_token => complete_token)
    @session.save!
    cookies.permanent[:user_token] = @user_token
    cookies.permanent[:session_id] = session_id
   
    AuthMailer.auth_email(params[:session][:email].downcase, @email_token).deliver
  end

  def waitForLogin
    render :json => { :result => signed_in? }
  end

  def index
  end

  def authenticate
    if request.post?
      user_token = params[:login][:user_token]
      email_token = params[:login][:email_token]
      validation_code = params[:login][:validation_code]

      if log_in(user_token, email_token, validation_code)
        if !cookies.permanent[:session_id].nil? && !current_user.nil?
          redirect_to current_user
        else
          if current_user.auth_secret.nil?
            return render json: current_user.assign_auth_secret
          else
            return render json: "success" 
          end
        end
      else
        return render json: "failure" 
      end
    else
      user_token = cookies.permanent[:user_token]
      @user = get_user(user_token)
      email_token = params[:email_token]
      if(!@user.auth_secret.nil?)
        @email_token = email_token
        @user_token = user_token
        return
      end

      if log_in(user_token, email_token) && !current_user.nil?
        redirect_to current_user 
      else
        redirect_to :new_session
      end
    end
  end
end
