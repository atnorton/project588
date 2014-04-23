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

  def confirm
    if request.post?
      @user = User.find_by(handle: params[:confirm][:user_handle])
      if verify_recaptcha
        auth_request(@user, @user.pending_email)
        render 'create'
      end
    else
      @user = User.find_by(handle: params[:handle])
      if(@user.nil? || @user.confirmed)
        redirect_to :new_session
      end
    end
  end

  def create
    clean_up
    @user = User.find_by(email: params[:session][:email].downcase)
    if(@user==nil)
      # If the user is not registered, don't send the email but don't
      # give any indication that are not member
      @user_token = SecureRandom.base64(32)
      session_id = SecureRandom.urlsafe_base64(32)

      cookies.permanent[:user_token] = { value: @user_token, httponly: true }
      cookies.permanent[:session_id] = { value: session_id, httponly: true }

      # Make the timing of success and failure look the same
      sleep(2)
      return
    end

    if lock_account?(@user)
      lock(@user)
    end

    auth_request(@user, @user.email)
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
