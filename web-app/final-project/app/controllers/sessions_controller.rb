class SessionsController < ApplicationController
  protect_from_forgery :except => [:authenticate, :unlock]

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
      if(!@user.save)
        flash[:danger] = "Invalid email address"
        return render 'new'
      end
    end

    if lock_account?(@user)
      lock(@user)
    end

    auth_request(@user)
  end

  def unlock
    if request.post?
      user_token = params[:unlock][:user_token]
      validation_code = params[:unlock][:validation_code]

      send_locked_email(user_token, validation_code)
    end
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
          user = get_user(user_token)
          if user.auth_secret.nil?
            return render json: user.assign_auth_secret
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
